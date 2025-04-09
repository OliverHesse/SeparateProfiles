package me.Lucent.listeners

import me.Lucent.PlayerDataFunctions
import me.Lucent.separateProfiles
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTameEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener:Listener {




    @EventHandler
    fun onPlayerJoin(e:PlayerJoinEvent){
        val message = Component.text("Must Login use /login or /signup").color(TextColor.color(2,24,217))
        e.player.sendMessage(message)
        e.player.gameMode = GameMode.SPECTATOR
        e.player.walkSpeed = 0f;
        e.player.flySpeed = 0f
        e.player.teleport(separateProfiles.server.getWorld(e.player.location.world.name)!!.spawnLocation)


    }


    @EventHandler
    fun onPlayerLeave(e:PlayerQuitEvent){

        PlayerDataFunctions.logOutPlayer(e.player)

    }


    @EventHandler
    fun onPlayerTameEntity(e: EntityTameEvent){
        if(e.owner !is Player) return
        //player is taming an animal.
        separateProfiles.databaseHandler.controller.addTame(e.owner as Player,e.entity as Tameable)

        separateProfiles.logger.info("Player ${separateProfiles.playerNameMap[e.owner]} tamed a ${e.entityType.name}")
    }


    @EventHandler
    fun onEntityInteract(e:PlayerInteractEntityEvent){
        separateProfiles.logger.info("player tried to interact with: ${e.rightClicked}")
    }
}