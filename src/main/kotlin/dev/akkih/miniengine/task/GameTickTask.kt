package dev.akkih.miniengine.task

import dev.akkih.miniengine.Game
import org.bukkit.scheduler.BukkitRunnable

/**
 * Runs every game tick for the current phase.
 */
class GameTickTask(private val game: Game) : BukkitRunnable() {
    override fun run() {
        try {
            game.tick()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}