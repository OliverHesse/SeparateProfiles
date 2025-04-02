package me.Lucent.commands

import me.Lucent.separateProfiles
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SignUpCommand:CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if(sender !is Player) return true;
        val player = sender as Player;
        if(args.size != 2){
            player.sendMessage("§cUsername and password cannot contain spaces")
            player.sendMessage("§cUsage: /signup <username> <password>")
            return true
        }
        val controller = separateProfiles.databaseHandler.controller
        if(!controller.validUsername(args[0])){
            player.sendMessage("§cUsername already exists or is not valid")
            return true
        }


        controller.createNewUser(player,args[0],args[1])
        player.sendMessage("§aAccount created")
        separateProfiles.logger.info("Account ${args[0]} created")
        return true
    }
}