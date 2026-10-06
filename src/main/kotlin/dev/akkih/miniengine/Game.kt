package dev.akkih.miniengine

import dev.akkih.miniengine.arena.Arena
import dev.akkih.miniengine.arena.ArenaFactory
import dev.akkih.miniengine.arena.ArenaTemplate
import dev.akkih.miniengine.arena.strategy.ArenaResetStrategy
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.event.definition.GameJoinEvent
import dev.akkih.miniengine.event.definition.GameLeaveEvent
import dev.akkih.miniengine.event.definition.GamePlayerEliminatedEvent
import dev.akkih.miniengine.state.GamePhase
import dev.akkih.miniengine.state.definition.GameCountdownPhase
import dev.akkih.miniengine.state.definition.GameRecruitingPhase
import dev.akkih.miniengine.state.definition.GameLoadingPhase
import dev.akkih.miniengine.state.definition.GameEndingPhase
import dev.akkih.miniengine.state.definition.GameActivePhase
import dev.akkih.miniengine.task.GameTickTask
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.event.player.PlayerEvent
import org.bukkit.event.world.WorldEvent
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID

/**
 * Manages the lifecycle, player tracking, and state transitions of a minigame.
 */
abstract class Game(
    val plugin: JavaPlugin,
    val template: ArenaTemplate,
    val arenaFactory: ArenaFactory,
    val resetStrategy: ArenaResetStrategy,
    val lobbyLocation: Location,
) : Listener {
    /**
     * The [GamePhase] this game instance is currently in.
     */
    var currentPhase: GamePhase = GameLoadingPhase
        private set

    /**
     * The active arena instance. Initialized during [GameLoadingPhase].
     */
    lateinit var arena: Arena

    /**
     * Checks whether the arena world has completed loading and is ready for use.
     */
    val isArenaInitialized: Boolean
        get() = this::arena.isInitialized

    /**
     * Elapsed ticks in the current phase.
     */
    var phaseTimer: Int = 0
        private set

    /**
     * The [BukkitRunnable]'s task for the current game phase.
     */
    private var tickTask: BukkitTask? = null

    /**
     * The listener for this game.
     */
    val gameListener = GameListener(this)

    /**
     * The alive players in this game instance.
     */
    val players = mutableSetOf<UUID>()

    /**
     * The eliminated players in this game instance.
     */
    val spectators = mutableSetOf<UUID>()

    /**
     * The players who won the match. Populated when the game transitions to an end state.
     */
    val winners = mutableSetOf<UUID>()

    /**
     * The minimum amount of players required to start the game.
     */
    abstract val minPlayers: Int

    /**
     * The maximum amount of players allowed in this game.
     */
    abstract val maxPlayers: Int

    /**
     * Runs whenever the [GameActivePhase] is triggered.
     */
    open fun onGameStart() {}

    /**
     * Runs every tick for [GameActivePhase].
     */
    open fun onGameTick() {}

    /**
     * Runs whenever the [GameEndingPhase] is triggered. You should declare the winners here.
     */
    open fun onGameEnd() {}

    /**
     * Starts the recruiting phase tick task and executes the state enter method.
     */
    fun start() {
        if (tickTask != null) return

        Bukkit.getPluginManager().registerEvents(gameListener, plugin)

        phaseTimer = 0
        currentPhase.onEnter(this)
        tickTask = GameTickTask(this).runTaskTimer(plugin, 0, 20L)
    }

    /**
     * Stops the current tick task and leaves the current state.
     */
    fun stop() {
        HandlerList.unregisterAll(gameListener)

        tickTask?.cancel()
        tickTask = null
        currentPhase.onLeave(this)

        (players + spectators).forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let { player ->
                cleanPlayer(player)

                if (player.isOnline) {
                    player.teleport(lobbyLocation)
                }
            }
        }

        players.clear()
        spectators.clear()
        winners.clear()
    }

    /**
     * Executes the current phase's game tick.
     */
    fun tick() {
        phaseTimer++
        currentPhase.onTick(this)
    }

    /**
     * Cleans up the current game instance to prepare for recruiting phase again.
     */
    open fun reset() {
        players.clear()
        spectators.clear()
        winners.clear()
    }

    /**
     * Adds a player to this game instance, only if:
     *
     * - The [currentPhase] is recruiting or in countdown;
     * - The amount of players is less than the [maxPlayers].
     *
     * @param player The player to add.
     *
     * @return true if the players is successfully added to the game instance.
     */
    fun join(player: Player): Boolean {
        if (players.size >= maxPlayers || (currentPhase !is GameRecruitingPhase && currentPhase !is GameCountdownPhase))
            return false

        players.add(player.uniqueId)
        cleanPlayer(player)
        player.gameMode = GameMode.ADVENTURE
        player.teleport(arena.lobbySpawn)

        if (currentPhase is GameRecruitingPhase && players.size >= minPlayers) {
            sendEvent(GameEvent.GameCountdownEvent)
        }

        Bukkit.getPluginManager().callEvent(GameJoinEvent(player, this))

        return true
    }

    /**
     * Eliminates and cleans up the given player, removing it from
     * the players set and adding it to the spectators set.
     */
    fun eliminate(player: Player) {
        players.remove(player.uniqueId)
        spectators.add(player.uniqueId)
        cleanPlayer(player)

        Bukkit.getPluginManager().callEvent(GamePlayerEliminatedEvent(player, this))
    }

    /**
     * Removes the player from the game, transitioning back to [GameRecruitingPhase]
     * if the remaining player count drops below [minPlayers] and if the game is not active.
     *
     * @param player The player to remove.
     */
    fun leave(player: Player) {
        val wasInGame = players.remove(player.uniqueId) || spectators.remove(player.uniqueId)
        if (!wasInGame) return

        cleanPlayer(player)

        if (player.isOnline) {
            player.teleport(lobbyLocation)
        }

        if (players.size < minPlayers && currentPhase !is GameRecruitingPhase) {
            sendEvent(GameEvent.GameRecruitingEvent)
        }

        when (currentPhase) {
            is GameRecruitingPhase, GameCountdownPhase -> {
                Bukkit.getPluginManager().callEvent(GameLeaveEvent(player, this))
            }

            is GameActivePhase -> {
                Bukkit.getPluginManager().callEvent(GamePlayerEliminatedEvent(player, this))
            }
        }
    }

    /**
     * Teleports all active players to individual spawn points.
     */
    fun distributePlayersToSpawns() {
        if (!isArenaInitialized) return

        arena.resetSpawns()
        players.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.teleport(arena.nextSpawnPoint())
        }
    }

    /**
     * Triggers an event to the current game instance. If the given event is responsible for changing the phase, the transition will be executed.
     *
     * @param event The game event to trigger.
     * @see GameEvent
     */
    fun sendEvent(event: GameEvent) {
        val newPhase = currentPhase.nextPhase(event)

        if (newPhase != currentPhase) {
            currentPhase.onLeave(this)
            currentPhase = newPhase
            phaseTimer = 0
            currentPhase.onEnter(this)
        }
    }

    /**
     * Resets health, hunger, fire, and potion status on a player.
     */
    fun cleanPlayer(player: Player, gameMode: GameMode = GameMode.ADVENTURE) {
        player.inventory.clear()
        player.clearTitle()
        player.gameMode = gameMode
        player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
        player.foodLevel = 20
        player.saturation = 5.0f
        player.fireTicks = 0
        player.fallDistance = 0f
        player.activePotionEffects.forEach { effect ->
            player.removePotionEffect(effect.type)
        }
    }

    /**
     * Executes [cleanPlayer] on all players.
     */
    fun cleanAllPlayers(gameMode: GameMode = GameMode.ADVENTURE) {
        broadcast {
            cleanPlayer(this, gameMode)
        }
    }

    /**
     * Executes an action for all connected participants (both active players and spectators).
     *
     * @param action The player action block to execute.
     */
    inline fun broadcast(action: Player.() -> Unit) {
        (players + spectators).forEach { uuid ->
            Bukkit.getPlayer(uuid)?.action()
        }
    }

    /**
     * Sends a message to all players in this game using [broadcast].
     *
     * @param message The message to send.
     */
    fun message(message: String) {
        broadcast {
            sendRichMessage(message)
        }
    }

    /**
     * Plays a sound to all players in this game using [broadcast].
     *
     * @param sound The Bukkit sound to play.
     * @param volume What volume the sound should be played. Default is `1f`.
     * @param pitch What pitch the sound should be played. Default is `1f`.
     */
    fun sound(sound: Sound, volume: Float = 1f, pitch: Float = 1f) {
        broadcast {
            playSound(this, sound, volume, pitch)
        }
    }

    /**
     * Checks whether the player is still participating in the game.
     */
    fun isParticipating(player: Player): Boolean {
        return players.contains(player.uniqueId) || spectators.contains(player.uniqueId)
    }

    /**
     * Registers an event to given [GamePhase]s.
     */
    inline fun <reified E : Event> on(
        vararg phases: GamePhase,
        priority: EventPriority = EventPriority.NORMAL,
        ignoreCancelled: Boolean = false,
        crossinline handler: E.() -> Unit
    ) {
        val executor = EventExecutor { _, event ->
            if (event !is E || currentPhase !in phases) return@EventExecutor

            val player = resolvePlayerFromEvent(event)
            if (player != null && !isParticipating(player)) return@EventExecutor

            if (isArenaInitialized && !isEventInArena(event)) return@EventExecutor

            handler(event)
        }

        Bukkit.getPluginManager().registerEvent(
            E::class.java,
            gameListener,
            priority,
            executor,
            plugin,
            ignoreCancelled
        )
    }

    /**
     * Resolves a [Player] instance from a Bukkit event.
     */
    fun resolvePlayerFromEvent(event: Event): Player? = when (event) {
        is PlayerEvent -> event.player
        is EntityDamageByEntityEvent -> (event.damager as? Player) ?: (event.entity as? Player)
        is EntityEvent -> event.entity as? Player
        is InventoryEvent -> event.view.player as? Player
        else -> null
    }

    /**
     * Checks if a given Bukkit event happened inside the arena instance.
     */
    fun isEventInArena(event: Event): Boolean {
        if (!isArenaInitialized) return false
        return when (event) {
            is BlockEvent -> event.block.world == arena.world
            is WorldEvent -> event.world == arena.world
            else -> true
        }
    }
}