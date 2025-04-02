package me.Lucent.commands

import me.Lucent.separateProfiles
import net.kyori.adventure.text.Component
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable

class LogInCommand:CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if(sender !is Player) return true;
        val player = sender as Player;
        if(args.size != 2){
            player.sendMessage("§cUsername and password cannot contain spaces")
            player.sendMessage("§cUsage: /login <username> <password>")
            return true
        }
        val controller = separateProfiles.databaseHandler.controller
        if(!controller.verifyUser(args[0],args[1])){
            player.sendMessage("§cUsername or password incorrect")
            return true
        }
        if(separateProfiles.playerNameMap[player] == null){
            player.sendMessage("§cAlready logged in to an account please log out first")
        }
        player.inventory.clear();
        player.enderChest.clear();


        player.displayName(Component.text(args[0]))


        //load users location data
        player.respawnLocation = controller.getUserRespawnLocation(player);
        player.teleport(controller.getUserLastLocation(player));

        //load player inventory data
        val inventoryItems = controller.getInventory(player)
        val enderChestData = controller.getEnderChest(player)

        for(itemData in inventoryItems){
            player.inventory.setItem(itemData.first,itemData.second)
        }
        for(itemData in enderChestData){
            player.enderChest.setItem(itemData.first,itemData.second)
        }


        //load xp data
        player.totalExperience= controller.getUserXP(player)

        //load tames data
        val tameData =controller.getUsersTames(player);

        for(tame in tameData){
            val typeString = tame.first
            val tameID = tame.second

            //i will need to loop through all worlds till i get a match
            //there is potential for there to be 2 wolves with the same ID but ill think of a fix later
            var found = false
            for(world in separateProfiles.server.worlds){
                val entity = world.getEntity(tameID) ?: continue
                if(entity.type != EntityType.valueOf(typeString)) continue
                found = true
                (entity as Tameable).owner = player
            }
            if(found) {
                player.sendMessage("§cProblem loading tames")
                separateProfiles.logger.severe("Problem loading tames for player with username ${args[0]}")
            }
        }


        return true
    }
}