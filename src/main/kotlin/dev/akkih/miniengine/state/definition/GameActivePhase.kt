package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase

object GameActivePhase : GamePhase {
    override val name = "Active"

    override val allowPVP = true

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameEndingEvent -> GameEndingPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        game.onGameStart()
    }

    override fun onTick(game: Game) {
        game.onGameTick()

        if (game.players.isEmpty()) {
            game.sendEvent(GameEvent.GameEndingEvent)
        }
    }
}
