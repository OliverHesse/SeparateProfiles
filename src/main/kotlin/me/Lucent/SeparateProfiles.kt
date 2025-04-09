package me.Lucent

import me.Lucent.commands.LogInCommand
import me.Lucent.commands.LogOutCommand
import me.Lucent.commands.SignUpCommand
import me.Lucent.database.DatabaseHandler
import me.Lucent.listeners.PlayerListener
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


//TODO Current Task:
//TODO add logout feature
//TODO fix bugs



val separateProfiles :SeparateProfiles
    get() = JavaPlugin.getPlugin(SeparateProfiles::class.java)

class SeparateProfiles : JavaPlugin() {
    lateinit var databaseHandler: DatabaseHandler
    val playerNameMap:HashMap<Player,String> = HashMap()

    fun isLoggedIn(username:String):Boolean{
        for(name in playerNameMap.values){
            if (name == username) return true;
        }
        return false
    }


    override fun onEnable() {
        if(!dataFolder.exists()) dataFolder.mkdir()
        databaseHandler = DatabaseHandler()
        databaseHandler.init()

        server.pluginManager.registerEvents(PlayerListener(),this)
        getCommand("signup")!!.setExecutor(SignUpCommand())
        getCommand("login")!!.setExecutor(LogInCommand())
        getCommand("logout")!!.setExecutor(LogOutCommand())
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
