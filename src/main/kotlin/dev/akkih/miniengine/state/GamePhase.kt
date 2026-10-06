package dev.akkih.miniengine.state

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.GameEvent
import org.bukkit.event.Event

/**
 * Represents a phase the game instance can be in.
 */
interface GamePhase {
    /**
     * This phase's identifier.
     */
    val name: String

    /**
     * Represents what event triggers certain phases in the game.
     *
     * @param event The generic GameEvent to be triggered.
     *
     * @return The phase for the event trigger.
     */
    fun nextPhase(event: GameEvent): GamePhase

    /**
     * Runs whenever this phase is triggered.
     *
     * @param game The parent Game instance.
     */
    fun onEnter(game: Game) {}

    /**
     * Runs every game tick once this phase is triggered.
     *
     * @param game The parent Game instance.
     */
    fun onTick(game: Game) {}

    /**
     * Runs whenever a new phase is triggered and the transition is happening.
     *
     * @param game The parent Game instance.
     */
    fun onLeave(game: Game) {}

    /**
     * Handles incoming Bukkit events for this specific phase.
     */
    fun handleEvent(game: Game, event: Event) {}

    /**
     * Allow players to damage each other in this phase.
     */
    val allowPVP: Boolean get() = false

    /**
     * Allow players to break blocks in this phase.
     */
    val allowBlockBreak: Boolean get() = false

    /**
     * Allow players to place blocks in this phase.
     */
    val allowBlockPlace: Boolean get() = false

    /**
     * Allow players saturation levels to decrease in this phase.
     */
    val allowHunger: Boolean get() = false
}