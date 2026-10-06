package dev.akkih.miniengine.event.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.CustomEvent
import org.bukkit.entity.Player

/**
 * Triggered when a player leaves a [Game].
 */
class GameLeaveEvent(val player: Player, val game: Game) : CustomEvent()