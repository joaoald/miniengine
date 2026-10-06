package dev.akkih.miniengine.event

/**
 * Represents all generic events that can happen in a game.
 *
 * In order of execution, the events are:
 * - [GameRecruitingEvent]
 * - [GameCountdownEvent]
 * - [GameActiveEvent]
 * - [GameEndingEvent]
 * - [GameResettingEvent]
 * - [GameReadyEvent]
 */
sealed interface GameEvent {
    /**
     * The game is currently recruiting players.
     */
    data object GameRecruitingEvent : GameEvent

    /**
     * The game is currently on countdown to start.
     *
     * Does not necessarily mean that the game instance is full.
     */
    data object GameCountdownEvent : GameEvent

    /**
     * The game has started and players are able to play.
     */
    data object GameActiveEvent : GameEvent

    /**
     * The game has ended.
     */
    data object GameEndingEvent : GameEvent

    /**
     * The game instance is cleaning up to go back to the recruiting phase.
     */
    data object GameResettingEvent : GameEvent

    /**
     * The game can go back to the recruiting phase.
     */
    data object GameReadyEvent : GameEvent
}