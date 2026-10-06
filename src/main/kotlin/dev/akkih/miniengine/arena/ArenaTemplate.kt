package dev.akkih.miniengine.arena

import dev.akkih.miniengine.util.RelativePoint
import org.bukkit.World
import org.bukkit.util.BoundingBox

/**
 * Static configuration for an arena loaded from an ASP template.
 */
data class ArenaTemplate(
    val id: String,
    val templateName: String,
    val lobbySpawn: RelativePoint,
    val spawnPoints: List<RelativePoint>,
    val bounds: BoundingBox
) {
    /**
     * Binds this template to a freshly loaded Bukkit [World] to create an [Arena] instance.
     */
    fun instantiate(world: World): Arena {
        return Arena(id, world, lobbySpawn.toLocation(world), spawnPoints.map { it.toLocation(world) }, bounds)
    }
}