package dev.akkih.miniengine.event.definition

import dev.akkih.miniengine.Game
import dev.akkih.miniengine.event.CustomEvent
import org.bukkit.entity.Player

/**
 * Triggered when a player gets eliminated from a [Game].
 */
class GamePlayerEliminatedEvent(val player: Player, val game: Game) : CustomEvent()