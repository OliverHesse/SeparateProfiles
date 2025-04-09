package me.Lucent

import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import java.util.UUID

object PlayerDataFunctions {

    fun getTame(player:Player,tameID:UUID,tameType:String):Tameable?{


        for(world in separateProfiles.server.worlds){
            val entity = world.getEntity(tameID) ?: continue
            if(entity.type != EntityType.valueOf(tameType)) continue
            return entity as Tameable
        }

        player.sendMessage("§could not find tame with ID $tameID")
        separateProfiles.logger.severe("could not find tame with ID $tameID for player $player ")
        return null
    }

    fun logOutPlayer(player: Player){
        if(separateProfiles.playerNameMap[player] == null){
            player.sendMessage("§cYou must be logged in to log out")
            return
        }
        val controller = separateProfiles.databaseHandler.controller
        if(!controller.updateExistingUser(player)){
            separateProfiles.logger.severe("Unable to update basic user info for player ${separateProfiles.playerNameMap[player]}")
        }

        if(!controller.updatePlayerInventory(player)){
            separateProfiles.logger.severe("Unable to save inventory for player ")
        }

        if(!controller.updatePlayerEnderChest(player)){
            separateProfiles.logger.severe("Unable to save enderchest for player ")
        }
        //remove any "invalid tames
        controller.removeInvalidTames(player);

        //set player tame owner to null
        val tames = controller.getUsersTames(player)
        for(tameData in tames){
            val tame = getTame(player,tameData.second,tameData.first)!!
            tame.owner = null

        }
        player.sendMessage("§aYou have logged out")
        separateProfiles.playerNameMap.remove(player)
    }

}