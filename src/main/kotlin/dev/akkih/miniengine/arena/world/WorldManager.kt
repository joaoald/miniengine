package dev.akkih.miniengine.arena.world

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI
import com.infernalsuite.asp.api.loaders.SlimeLoader
import com.infernalsuite.asp.api.world.properties.SlimeProperties
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * Represents the world loader using [ASP](https://github.com/InfernalSuite/AdvancedSlimePaper).
 */
class WorldManager(private val plugin: JavaPlugin, private val slime: AdvancedSlimePaperAPI, private val loader: SlimeLoader) {
    /**
     * Creates a [World] instance asynchronously.
     * Automatically loads using ASP and allows for it to be used as soon as it is completed.
     *
     * @param templateName The .slime template name.
     * @param instanceName The world instance name. (optional)
     *
     * @return A [CompletableFuture] for the given world.
     */
    fun createInstanceAsync(
        templateName: String,
        instanceName: String = "game_${templateName}_${UUID.randomUUID().toString().take(8)}"
    ): CompletableFuture<World> {
        val future = CompletableFuture<World>()

        val properties = SlimePropertyMap().apply {
            setValue(SlimeProperties.DIFFICULTY, "normal")
            setValue(SlimeProperties.ALLOW_MONSTERS, false)
            setValue(SlimeProperties.ALLOW_ANIMALS, false)
            setValue(SlimeProperties.PVP, true)
        }

        Bukkit.getAsyncScheduler().runNow(plugin) { _ ->
            try {
                val templateWorld = slime.readWorld(loader, templateName, true, properties)
                val clonedWorld = templateWorld.clone(instanceName, null)

                Bukkit.getGlobalRegionScheduler().run(plugin) { _ ->
                    try {
                        slime.loadWorld(clonedWorld, true)

                        val bukkitWorld = Bukkit.getWorld(instanceName)
                            ?: throw IllegalStateException("Failed to load generated world '$instanceName'")

                        bukkitWorld.isAutoSave = false
                        future.complete(bukkitWorld)
                    } catch (ex: Exception) {
                        plugin.logger.severe("[ASP] Failed during Bukkit world generation: ${ex.message}")
                        ex.printStackTrace()
                        future.completeExceptionally(ex)
                    }
                }
            } catch (ex: Exception) {
                plugin.logger.severe("[ASP] Failed while reading or cloning template: ${ex.message}")
                ex.printStackTrace()
                future.completeExceptionally(ex)
            }
        }

        return future
    }

    /**
     * Unloads and destroys an active arena world instance without saving changes.
     *
     * @param world The world to unload.
     * @return True if the world was successfully unloaded, false otherwise.
     */
    fun destroyInstance(world: World): Boolean {
        return Bukkit.unloadWorld(world, false)
    }
}