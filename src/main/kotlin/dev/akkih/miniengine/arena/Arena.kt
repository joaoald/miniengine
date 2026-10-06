package dev.akkih.miniengine.arena

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.BoundingBox

/**
 * Configuration and spatial boundaries for an active minigame arena instance.
 *
 * @property id The unique identifier of this arena.
 * @property world The Bukkit world where this arena resides.
 * @property lobbySpawn The waiting area where players spawn before the match begins.
 * @property spawnPoints The spawn locations cycled through during game start.
 * @property bounds The cuboid bounding box defining the playable area.
 */
data class Arena(
    val id: String,
    val world: World,
    val lobbySpawn: Location,
    val spawnPoints: List<Location>,
    val bounds: BoundingBox
) {
    /**
     * The index for the next spawn location.
     */
    private var nextSpawnIndex = 0

    /**
     * Returns the next available spawn point cycling through [spawnPoints].
     */
    fun nextSpawnPoint(): Location {
        if (spawnPoints.isEmpty()) return lobbySpawn
        val spawn = spawnPoints[nextSpawnIndex % spawnPoints.size]
        nextSpawnIndex++
        return spawn
    }

    /**
     * Checks if a player or location is within the arena boundaries.
     *
     * @param location The location to check for.
     *
     * @return True if the given location is within the arena boundaries.
     */
    fun contains(location: Location): Boolean {
        return location.world == world && bounds.contains(location.toVector())
    }

    /**
     * Resets the spawn index.
     */
    fun resetSpawns() {
        nextSpawnIndex = 0
    }
}