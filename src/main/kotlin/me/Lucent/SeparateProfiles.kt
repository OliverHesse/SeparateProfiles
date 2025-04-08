package me.Lucent

import me.Lucent.commands.LogInCommand
import me.Lucent.commands.SignUpCommand
import me.Lucent.database.DatabaseHandler
import me.Lucent.listeners.PlayerListener
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


//TODO Current Task:
//TODO save Tames when tamed(i have to wait till i get home for this)



val separateProfiles :SeparateProfiles
    get() = JavaPlugin.getPlugin(SeparateProfiles::class.java)

class SeparateProfiles : JavaPlugin() {
    lateinit var databaseHandler: DatabaseHandler
    val playerNameMap:HashMap<Player,String> = HashMap()




    override fun onEnable() {
        if(!dataFolder.exists()) dataFolder.mkdir()
        databaseHandler = DatabaseHandler()
        databaseHandler.init()

        server.pluginManager.registerEvents(PlayerListener(),this)
        getCommand("signup")!!.setExecutor(SignUpCommand())
        getCommand("login")!!.setExecutor(LogInCommand())
        // Plugin startup logic
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
