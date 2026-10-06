package dev.akkih.miniengine.util

import org.bukkit.Location
import org.bukkit.World

/**
 * Represents a [Location] without a specific [World] configuration.
 *
 * Can be converted to a World using [toLocation].
 */
data class RelativePoint(val x: Double, val y: Double, val z: Double, val yaw: Float = 0f, val pitch: Float = 0f) {
    /**
     * Converts the current point to a [Location].
     */
    fun toLocation(world: World) = Location(world, x, y, z, yaw, pitch)
}