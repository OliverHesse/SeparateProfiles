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




}