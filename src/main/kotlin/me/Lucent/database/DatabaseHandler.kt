package me.Lucent.database


import me.Lucent.database.Controllers.SQLiteController
import me.Lucent.separateProfiles
import java.sql.Connection
import java.sql.DriverManager
import java.sql.JDBCType

class DatabaseHandler(){


    lateinit var sqlConnection: Connection;
    val controller = SQLiteController();

    fun init(){
        separateProfiles.saveResource("separateProfiles-data.db",false)
        if(separateProfiles.dataFolder.exists()){
            try{
                val dbConnection = DriverManager.getConnection("jdbc:sqlite:${separateProfiles.dataFolder.absolutePath}\\separateProfiles-data.db")
                sqlConnection = dbConnection!!;
            }catch (e:Exception){
                separateProfiles.logger.info("could not connect to database")
                e.printStackTrace()
            }
        }else{
            separateProfiles.logger.severe("no Data folder found")
            separateProfiles.server.pluginManager.disablePlugin(separateProfiles)
        }
        controller.createTables()
    }



}