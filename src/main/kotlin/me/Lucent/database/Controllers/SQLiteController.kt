package me.Lucent.database.Controllers

import at.favre.lib.crypto.bcrypt.BCrypt
import me.Lucent.separateProfiles
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.UUID

class SQLiteController {

    //functions used to retrieve data from db

    fun getUserLastLocation(player: Player):Location{

        return Location(player.world,0.0,0.0,0.0)
    }

    fun getUserRespawnLocation(player: Player):Location?{
        return player.respawnLocation!!;
    }

    fun getUserXP(player: Player):Int{
        return 0
    }

    fun getUsersTames(player: Player):List<Pair<String,UUID>>{

        return emptyList()
    }

    fun getInventory(player: Player):List<Pair<Int,ItemStack>>{
        return emptyList()
    }

    fun getEnderChest(player:Player):List<Pair<Int, ItemStack>>{
        return emptyList()
    }

    //functions used for verification

    fun verifyUser(username:String,password:String):Boolean{


        return true
    }

    fun validUsername(username:String):Boolean{
        val stmtString = "SELECT * FROM users WHERE username = ?"
        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString);
        stmt.setString(1,username)
        try {
            val rs = stmt.executeQuery();
            val n = rs.next()
            separateProfiles.logger.info("is valid username $n")
            return !n
        } catch (e:Exception){

            separateProfiles.logger.severe("Error trying to validate username $username");
            e.printStackTrace();
            return false
        }
        return true;
    }

    //function used to update db

    fun updatePlayerInventory(player: Player){}

    fun updatePlayerEnderChest(player: Player){}

    fun createNewUser(player:Player,username: String,password: String):Boolean{
        val hashed = BCrypt.withDefaults().hashToString(12,password.toCharArray()) ?: return false;
        val loc = player.location.toVector()
        val locWorld = player.location.world.name

        val spawnLocation = player.respawnLocation
        var spawnLocationExists: Int = 1
        val spawnLoc = spawnLocation?.toVector() ?: Vector(0,0,0)
        val spawnWorld = spawnLocation?.world?.name ?: "N/A"
        val totalXP = player.totalExperience;
        if(spawnLocation == null) spawnLocationExists = 0

        val commandString = """
            INSERT INTO users 
            (username,password,lastLocX,lastLocY,lastLocZ,lastLocWorld,spawnLocX,spawnLocY,spawnLocZ,spawnWorld,noSpawnLocation,totalXP)
            VALUES
            (?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()

        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(commandString);
        stmt.setString(1,username);
        stmt.setString(2,hashed)
        stmt.setDouble(3, loc.x)
        stmt.setDouble(4, loc.y)
        stmt.setDouble(5, loc.z)
        stmt.setString(6,locWorld);
        stmt.setDouble(7,spawnLoc.x)
        stmt.setDouble(8,spawnLoc.y)
        stmt.setDouble(9,spawnLoc.z)
        stmt.setString(10,spawnWorld)
        stmt.setInt(11,spawnLocationExists)
        stmt.setInt(12,totalXP)
        try{
            stmt.executeUpdate()

        } catch (e:Exception){
            e.printStackTrace()
            return false
        }


        return true
    }

    fun updateExistingUser(player: Player){}

    fun addTame(tame: Tameable){}

    fun removeInvalidTames(player: Player){}

    //for now does not salt hashes
    fun createTables(){

        val userTable = """
            CREATE TABLE IF NOT EXISTS users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password TEXT NOT NULL,
                lastLocX REAL NOT NULL,
                lastLocY REAL NOT NULL,
                lastLocZ REAL NOT NULL,
                lastLocWorld TEXT NOT NULL,
                spawnLocX REAL NOT NULL,
                spawnLocY REAL NOT NULL,
                spawnLocZ REAL NOT NULL,
                spawnWorld TEXT NOT NULL,
                noSpawnLocation INTEGER NOT NULL,
                totalXP INTEGER NOT NULL
            )
        """.trimIndent()
        val tameTable = """
             CREATE TABLE IF NOT EXISTS tames(
                 id INTEGER PRIMARY KEY AUTOINCREMENT,
                 userID INTEGER NOT NULL,
                 tameId TEXT NOT NULL,
                 tameEntityType TEXT NOT NULL,
                 FOREIGN KEY(userID) REFERENCES users(id)
             )
        """.trimIndent()
        val inventoryTable = """
            CREATE TABLE IF NOT EXISTS inventoryItems(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                userID INTEGER NOT NULL,
                inventoryType TEXT NOT NULL,
                serializedItemStack BLOB NOT NULL,
                itemSlot INTEGER NOT NULL,
                FOREIGN KEY(userID) REFERENCES users(id)
            )
        """.trimIndent()

        try {
            val stmt =separateProfiles.databaseHandler.sqlConnection.createStatement()
            stmt.executeUpdate(userTable);
            stmt.executeUpdate(tameTable);
            stmt.executeUpdate(inventoryTable);


        }catch (e:Exception){
            separateProfiles.logger.severe("failed to create tables")
            separateProfiles.server.pluginManager.disablePlugin(separateProfiles)
            e.printStackTrace();
        }
    }
}