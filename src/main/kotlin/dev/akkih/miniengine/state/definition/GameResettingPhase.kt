package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase
import org.bukkit.Bukkit

object GameResettingPhase : GamePhase {
    override val name = "Resetting"

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameReadyEvent -> GameRecruitingPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        (game.players + game.spectators).forEach { uuid ->
            Bukkit.getPlayer(uuid)?.let { player ->
                game.cleanPlayer(player)
                player.teleport(game.lobbyLocation)
            }
        }

        game.resetStrategy.reset(game.arena).thenAccept { arena ->
            Bukkit.getGlobalRegionScheduler().run(game.plugin) { _ ->
                game.arena = arena
                game.reset()
                game.sendEvent(GameEvent.GameReadyEvent)
            }
        }.exceptionally { ex ->
            ex.printStackTrace()
            null
        }
    }
}