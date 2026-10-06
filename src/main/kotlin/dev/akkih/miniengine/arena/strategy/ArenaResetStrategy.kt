package dev.akkih.miniengine.arena.strategy

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.arena.Arena
import java.util.concurrent.CompletableFuture

/**
 * Represents the strategy to clean up [Game] instances after they are finished.
 */
interface ArenaResetStrategy {
    /**
     * Reset logic for [Arena].
     *
     * @param arena The Arena instance to reset.
     *
     * @return The modified Arena instance.
     */
    fun reset(arena: Arena): CompletableFuture<Arena>
}