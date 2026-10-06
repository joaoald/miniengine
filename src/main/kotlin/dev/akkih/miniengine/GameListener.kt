package dev.akkih.miniengine

import dev.akkih.miniengine.event.definition.GameJoinEvent
import dev.akkih.miniengine.event.definition.GameLeaveEvent
import dev.akkih.miniengine.state.definition.GameCountdownPhase
import dev.akkih.miniengine.state.definition.GameRecruitingPhase
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerQuitEvent

class GameListener(private val game: Game) : Listener {
    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (!game.isParticipating(event.player)) return

        if (!game.currentPhase.allowBlockBreak) {
            event.isCancelled = true
            return
        }

        game.currentPhase.handleEvent(game, event)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        if (!game.isParticipating(event.player)) return

        if (!game.currentPhase.allowBlockPlace) {
            event.isCancelled = true
            return
        }

        game.currentPhase.handleEvent(game, event)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return
        if (!game.isParticipating(player)) return

        if (player.uniqueId in game.spectators) {
            event.isCancelled = true
            return
        }

        if (event is EntityDamageByEntityEvent) {
            val damager = event.damager as? Player
            if (damager != null && (!game.currentPhase.allowPVP || damager.uniqueId in game.spectators)) {
                event.isCancelled = true
                return
            }
        }

        game.currentPhase.handleEvent(game, event)
    }

    @EventHandler
    fun onFoodLoss(event: FoodLevelChangeEvent) {
        val player = event.entity as? Player ?: return
        if (!game.isParticipating(player)) return

        if (!game.currentPhase.allowHunger) {
            event.isCancelled = true
            return
        }

        game.currentPhase.handleEvent(game, event)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (!game.isParticipating(event.player)) return

        game.leave(event.player)
    }

    @EventHandler
    fun onGameJoin(event: GameJoinEvent) {
        if (!game.isParticipating(event.player)) return

        if (game.currentPhase is GameRecruitingPhase || game.currentPhase is GameCountdownPhase) {
            game.message("<light_purple>${event.player.name} <yellow>juntou-se ao jogo. <green>[${game.players.size}/${game.maxPlayers}]")
        }
    }

    @EventHandler
    fun onGameLeave(event: GameLeaveEvent) {
        game.message("<light_purple>${event.player.name} <yellow>saiu do jogo. <red>[${game.players.size}/${game.maxPlayers}]")

        if (game.players.size < game.minPlayers) {
            game.message("<red>A contagem foi interrompida devido à quantidade de jogadores.")
        }
    }
}