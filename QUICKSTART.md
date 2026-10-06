# Quickstart

This guide shows how to configure an arena template, implement a minimal game class, and start it from a Paper plugin.

---

## 1. Define an Arena Template

An `ArenaTemplate` holds the template file name, waiting spawn, player spawns, and boundary box:

```kotlin
val template = ArenaTemplate(
    id = "arena_1",
    templateName = "arena_neon", // Matches arena_neon.slime inside your slime directory
    lobbySpawn = RelativePoint(0.5, 100.0, 0.5),
    spawnPoints = listOf(
        RelativePoint(15.0, 65.0, 0.0, -90f, 0f),
        RelativePoint(-15.0, 65.0, 0.0, 90f, 0f)
    ),
    bounds = BoundingBox(50.0, 150.0, 50.0, -50.0, 50.0, -50.0)
)
```

---

## 2. Implement the Game

Extend `Game` and provide minimum/maximum player counts. Use the lifecycle callbacks and phase-scoped event listeners:

```kotlin
package my.minigame

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.arena.ArenaFactory
import dev.akkih.miniengine.arena.ArenaTemplate
import dev.akkih.miniengine.arena.strategy.ArenaResetStrategy
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.definition.GameActivePhase
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.plugin.java.JavaPlugin

class SimpleGame(
    plugin: JavaPlugin,
    template: ArenaTemplate,
    arenaFactory: ArenaFactory,
    resetStrategy: ArenaResetStrategy,
    lobbyLocation: Location
) : Game(plugin, template, arenaFactory, resetStrategy, lobbyLocation) {

    override val minPlayers = 2
    override val maxPlayers = 8

    init {
        // Intercept lethal damage during active phase
        on<EntityDamageEvent>(GameActivePhase) {
            val player = entity as? Player ?: return@on

            if (player.health - finalDamage <= 0) {
                isCancelled = true
                eliminate(player)
            }
        }
    }

    override fun onGameStart() {
        message("<green>Fight!")
    }

    override fun onGameTick() {
        // End when only one player remains
        if (players.size <= 1) {
            winners.addAll(players)
            sendEvent(GameEvent.GameEndingEvent)
        }
    }

    override fun onGameEnd() {
        broadcast {
            val won = uniqueId in winners
            sendRichMessage(if (won) "<gold><bold>Victory!" else "<red><bold>Defeat!")
        }
    }
}
```

---

## 3. Hook into Your Plugin

Initialize the ASP file loader, create the `WorldManager` and `ArenaFactory`, and start the game instance:

```kotlin
package my.minigame

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.loaders.file.FileLoader
import dev.akkih.miniengine.Game
import dev.akkih.miniengine.arena.ArenaFactory
import dev.akkih.miniengine.arena.ArenaTemplate
import dev.akkih.miniengine.arena.strategy.definition.DefaultResetStrategy
import dev.akkih.miniengine.arena.world.WorldManager
import dev.akkih.miniengine.util.RelativePoint
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.BoundingBox
import java.io.File

class MyPlugin : JavaPlugin() {
    private lateinit var game: Game

    override fun onEnable() {
        val slime = AdvancedSlimePaperAPI.instance()
        val fileLoader = FileLoader(File("slime_worlds"))
        val worldManager = WorldManager(this, slime, fileLoader)
        val arenaFactory = ArenaFactory(worldManager)

        val serverLobby = Location(Bukkit.getWorld("world"), 0.5, 70.0, 0.5)

        val template = ArenaTemplate(
            id = "arena_1",
            templateName = "arena_neon",
            lobbySpawn = RelativePoint(0.5, 100.0, 0.5),
            spawnPoints = listOf(
                RelativePoint(15.0, 65.0, 0.0, -90f, 0f),
                RelativePoint(-15.0, 65.0, 0.0, 90f, 0f)
            ),
            bounds = BoundingBox(50.0, 150.0, 50.0, -50.0, 50.0, -50.0)
        )

        val resetStrategy = DefaultResetStrategy(this, template, worldManager, arenaFactory, serverLobby)

        game = SimpleGame(this, template, arenaFactory, resetStrategy, serverLobby).also {
            it.start() // Runs async world load and begins ticking
        }
    }

    override fun onDisable() {
        if (::game.isInitialized) {
            game.stop()
        }
    }
}
```

---

## 4. Player Join / Leave

Call `join(player)` and `leave(player)` from commands or menu handlers:

```kotlin
// In your /join command
if (!game.join(player)) {
    player.sendRichMessage("<red>Cannot join: game full or already running.")
}

// In your /leave command
if (game.isParticipating(player)) {
    game.leave(player)
}
```
