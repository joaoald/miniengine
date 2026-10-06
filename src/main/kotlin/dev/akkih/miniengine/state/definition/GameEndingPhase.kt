package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase

object GameEndingPhase : GamePhase {
    override val name = "Ending"

    private const val CELEBRATION_SECONDS = 10

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameResettingEvent -> GameResettingPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        game.onGameEnd()
    }

    override fun onTick(game: Game) {
        if (game.phaseTimer >= CELEBRATION_SECONDS) {
            game.sendEvent(GameEvent.GameResettingEvent)
        }
    }

    override fun onLeave(game: Game) {
        game.broadcast {
            game.cleanPlayer(this)
        }
    }
}