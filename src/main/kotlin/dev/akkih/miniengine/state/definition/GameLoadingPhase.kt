package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase
import org.bukkit.Bukkit

object GameLoadingPhase : GamePhase {
    override val name = "Loading"

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameReadyEvent -> GameRecruitingPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        game.arenaFactory.createArenaAsync(game.template).thenAccept { arena ->
            Bukkit.getGlobalRegionScheduler().run(game.plugin) { _ ->
                game.arena = arena
                game.plugin.logger.info("Arena '${arena.id}' loaded!")
                game.sendEvent(GameEvent.GameReadyEvent)
            }
        }.exceptionally { ex ->
            game.plugin.logger.severe("Failed to load arena world for template '${game.template.id}': ${ex.message}")
            ex.printStackTrace()
            null
        }
    }
}