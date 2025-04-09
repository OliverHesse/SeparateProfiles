package me.Lucent.commands

import me.Lucent.PlayerDataFunctions
import me.Lucent.separateProfiles
import org.bukkit.GameMode
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LogOutCommand:CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        if(sender !is Player) return true
        if(args.isNotEmpty()) return true

        PlayerDataFunctions.logOutPlayer(sender)

        sender.gameMode = GameMode.SPECTATOR
        sender.walkSpeed = 0f;
        sender.flySpeed = 0f
        sender.teleport(separateProfiles.server.getWorld(sender .location.world.name)!!.spawnLocation)

        return true

    }
}