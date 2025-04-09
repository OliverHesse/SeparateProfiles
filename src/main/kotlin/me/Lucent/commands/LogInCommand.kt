package me.Lucent.commands

import me.Lucent.PlayerDataFunctions
import me.Lucent.separateProfiles
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ItemStack

class LogInCommand:CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {

        if(sender !is Player) return true;
        val player :Player = sender;
        if(args.size != 2){
            player.sendMessage("§cUsername and password cannot contain spaces")
            player.sendMessage("§cUsage: /login <username> <password>")
            return true
        }

        separateProfiles.logger.info("Player is trying to log into ${args[0]}")

        if(separateProfiles.isLoggedIn(args[0])){
            separateProfiles.logger.info("someone is already logged into ${args[0]}")
            player.sendMessage("§cSomeone is already Logged in to account ${args[0]}")
            return true

        }

        val controller = separateProfiles.databaseHandler.controller
        if(!controller.verifyUser(args[0],args[1])){
            player.sendMessage("§cUsername or password incorrect")
            return true
        }
        if(separateProfiles.playerNameMap[player] != null){
            player.sendMessage("§cAlready logged in to an account please log out first")
        }
        player.inventory.clear();
        player.enderChest.clear();


        player.displayName(Component.text(args[0]))


        //load users location data
        player.respawnLocation = controller.getUserRespawnLocation(args[0]);
        val location = controller.getUserLastLocation(args[0]);
        if (location == null){
            player.sendMessage("§cThere was a problem loading player ${args[0]} location")
            return true
        }
        player.teleport(location);

        //load xp data
        player.totalExperience= controller.getUserXP(args[0])


        player.walkSpeed = 0.1f
        player.gameMode = GameMode.CREATIVE; //TODO change to survival
        separateProfiles.playerNameMap[player] = args[0]
        //temp
        player.sendMessage("§aLogged in to account ${args[0]}")



        //load player inventory data
        val inventoryItems = controller.getInventory(player)



        for(itemData in inventoryItems){
            player.inventory.setItem(itemData.first,itemData.second)
        }

        val enderChestData = controller.getEnderChest(player)

        for(itemData in enderChestData){
            player.enderChest.setItem(itemData.first,itemData.second)
        }



        //first remove invalid tames
        controller.removeInvalidTames(player)
        //load tames data
        val tameData =controller.getUsersTames(player);

        for(tameData in tameData){

            val tame = PlayerDataFunctions.getTame(player, tameData.second,tameData.first)!!

            tame.owner = player;
            separateProfiles.logger.info("Player ${args[0]} tame of type ${tameData.first} has been loaded")
        }


        return true
    }
}