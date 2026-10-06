package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase

object GameRecruitingPhase : GamePhase {
    override val name = "Recruiting"

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameCountdownEvent -> GameCountdownPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        if (game.isArenaInitialized) {
            game.arena.resetSpawns()
        }
    }
}