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

        val controller = separateProfiles.databaseHandler.controller
        if(!controller.updateExistingUser(e.player)){
            separateProfiles.logger.severe("Unable to update basic user info for player ${separateProfiles.playerNameMap[e.player]}")
        }

        if(!controller.updatePlayerInventory(e.player)){
            separateProfiles.logger.severe("Unable to save inventory for player ")
        }

        if(!controller.updatePlayerEnderChest(e.player)){
            separateProfiles.logger.severe("Unable to save enderchest for player ")
        }
        //remove any "invalid tames
        controller.removeInvalidTames(e.player);

        //set player tame owner to null
        val tames = controller.getUsersTames(e.player)
        for(tameData in tames){
            val tame = PlayerDataFunctions.getTame(e.player,tameData.second,tameData.first)!!
            tame.owner = null

        }
        separateProfiles.playerNameMap.remove(e.player)

    }


    @EventHandler
    fun onPlayerTameEntity(e: EntityTameEvent){
        if(e.owner !is Player) return
        //player is taming an animal.
        separateProfiles.databaseHandler.controller.addTame(e.owner as Player,e.entity as Tameable)

        separateProfiles.logger.info("Player ${separateProfiles.playerNameMap[e.owner]} tamed a ${e.entityType.name}")
    }
}