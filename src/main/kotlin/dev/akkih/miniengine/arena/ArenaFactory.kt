package dev.akkih.miniengine.arena

import dev.akkih.miniengine.arena.world.WorldManager
import java.util.concurrent.CompletableFuture

/**
 * Creates an [Arena] based on the given [ArenaTemplate] configuration.
 */
class ArenaFactory(private val worldManager: WorldManager) {
    fun createArenaAsync(template: ArenaTemplate): CompletableFuture<Arena> {
        return worldManager.createInstanceAsync(template.templateName).thenApply { world ->
            template.instantiate(world)
        }
    }
}