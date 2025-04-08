package me.Lucent.listeners

import me.Lucent.separateProfiles
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityTameEvent
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

        if(!separateProfiles.databaseHandler.controller.updateExistingUser(e.player)){
            separateProfiles.logger.severe("Unable to update basic user info for player ${separateProfiles.playerNameMap[e.player]}")
        }
        //TODO save player inventory
        if(!separateProfiles.databaseHandler.controller.updatePlayerInventory(e.player)){
            separateProfiles.logger.severe("Unable to save inventory for player ")
        }

        separateProfiles.playerNameMap.remove(e.player)

    }


    @EventHandler
    fun onPlayerTameEntity(e: EntityTameEvent){
        if(e.entity !is Player) return



    }
}