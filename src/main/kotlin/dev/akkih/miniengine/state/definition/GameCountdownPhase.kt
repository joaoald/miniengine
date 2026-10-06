package dev.akkih.miniengine.state.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import dev.akkih.miniengine.state.GamePhase
import org.bukkit.Sound

object GameCountdownPhase : GamePhase {
    override val name = "Countdown"

    private const val REFERENCE_COUNTDOWN_SECONDS = 20
    var COUNTDOWN_SECONDS = 20

    override fun nextPhase(event: GameEvent): GamePhase {
        return when (event) {
            is GameEvent.GameActiveEvent -> GameActivePhase
            is GameEvent.GameRecruitingEvent -> GameRecruitingPhase
            else -> this
        }
    }

    override fun onEnter(game: Game) {
        COUNTDOWN_SECONDS = REFERENCE_COUNTDOWN_SECONDS
    }

    override fun onTick(game: Game) {
        val secondsRemaining = COUNTDOWN_SECONDS - game.phaseTimer

        if (secondsRemaining > 0) {
            if (secondsRemaining in setOf(15, 10, 5, 4, 3, 2, 1)) {
                val formattedSeconds = "$secondsRemaining segundo${if (secondsRemaining > 1) "s" else ""}"
                game.message("<yellow>O jogo irá começar em <light_purple>${formattedSeconds}<yellow>.")
                game.sound(Sound.BLOCK_NOTE_BLOCK_PLING)
            }

            return
        }

        game.message("<green>O jogo começou, boa sorte!")
        game.sound(Sound.ENTITY_PLAYER_LEVELUP)

        game.distributePlayersToSpawns()
        game.sendEvent(GameEvent.GameActiveEvent)
    }
}