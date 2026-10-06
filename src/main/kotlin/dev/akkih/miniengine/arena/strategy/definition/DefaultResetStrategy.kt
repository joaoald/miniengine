package dev.akkih.miniengine.arena.strategy.definition

import dev.akkih.miniengine.arena.Arena
import dev.akkih.miniengine.arena.ArenaFactory
import dev.akkih.miniengine.arena.ArenaTemplate
import dev.akkih.miniengine.arena.strategy.ArenaResetStrategy
import dev.akkih.miniengine.arena.world.WorldManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CompletableFuture

/**
 * The default reset strategy to clean up arenas.
 * It entirely unloads the world while keeping the template working fine.
 */
class DefaultResetStrategy(
    private val plugin: JavaPlugin,
    private val template: ArenaTemplate,
    private val worldManager: WorldManager,
    private val arenaFactory: ArenaFactory,
    private val lobbyLocation: Location,
) : ArenaResetStrategy {
    override fun reset(arena: Arena): CompletableFuture<Arena> {
        val future = CompletableFuture<Arena>()

        val evacuations = arena.world.players.map { player ->
            player.teleportAsync(lobbyLocation)
        }

        CompletableFuture.allOf(*evacuations.toTypedArray()).thenRun {
            Bukkit.getGlobalRegionScheduler().run(plugin) { _ ->
                val unloaded = worldManager.destroyInstance(arena.world)
                if (!unloaded) {
                    plugin.logger.warning("Failed to unload arena world '${arena.world.name}'!")
                }

                arenaFactory.createArenaAsync(template)
                    .thenAccept { newArena -> future.complete(newArena) }
                    .exceptionally { ex ->
                        future.completeExceptionally(ex)
                        null
                    }
            }
        }.exceptionally { ex ->
            future.completeExceptionally(ex)
            null
        }

        return future
    }
}