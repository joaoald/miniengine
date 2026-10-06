package dev.akkih.miniengine.event

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import java.time.Instant

/**
 * Creates a custom Bukkit event.
 */
open class CustomEvent : Event(), Cancellable {
    val timestamp: Instant = Instant.now()
    private var _cancelled: Boolean = false

    companion object {
        val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList() = HANDLERS
    }

    override fun getHandlers() = HANDLERS

    override fun isCancelled() = _cancelled
    override fun setCancelled(cancel: Boolean) { _cancelled = cancel }
}