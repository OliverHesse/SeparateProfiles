package me.Lucent.database.Controllers

import at.favre.lib.crypto.bcrypt.BCrypt
import me.Lucent.separateProfiles
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.*

class SQLiteController {

    //functions used to retrieve data from db
    //TODO Combine into one statement to reduce calls
    //TODO store that data in a playerSaveDataClass
    fun getUserLastLocation(username: String):Location?{
        val stmtString = """
            SELECT lastLocX,lastLocY,lastLocZ,lastLocWorld
            FROM users
            WHERE username = ?
        """.trimIndent()
        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString);
        stmt.setString(1, username)

        try{
            val rs = stmt.executeQuery();
            if(!rs.next()) throw NoSuchElementException("no location data found for player $username")
            val world = separateProfiles.server.getWorld(rs.getString("lastLocWorld"))
            return Location(world,rs.getDouble("lastLocX"),rs.getDouble("lastLocY"),rs.getDouble("lastLocZ"))
        }catch (e:Exception){
            separateProfiles.logger.severe("unable to get user $username location")
            e.printStackTrace()
        }

        return null
    }

    fun getUserRespawnLocation(username: String):Location?{
        val stmtString = """
            SELECT spawnLocX,spawnLocY,spawnLocZ,spawnWorld,noSpawnLocation
            FROM users
            WHERE username = ?
        """.trimIndent()
        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString);
        stmt.setString(1, username)

        try{
            val rs = stmt.executeQuery();
            if(!rs.next()) throw NoSuchElementException("no respawn data found for player $username")
            if(rs.getInt("noSpawnLocation") == 0) return null
            val world = separateProfiles.server.getWorld(rs.getString("spawnWorld"))
            return Location(world,rs.getDouble("spawnLocX"),rs.getDouble("spawnLocY"),rs.getDouble("spawnLocZ"))
        }catch (e:Exception){
            separateProfiles.logger.severe("unable to get user $username respawn location")
            e.printStackTrace()
        }

        return null;
    }

    fun getUserXP(username: String):Int{
        val stmtString = """
            SELECT totalXP
            FROM users
            WHERE username = ?
        """.trimIndent()
        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString);
        stmt.setString(1, username)

        try{
            val rs = stmt.executeQuery();
            if(!rs.next()) throw NoSuchElementException("no XP data found for player $username")

            return rs.getInt("totalXP")
        }catch (e:Exception){
            separateProfiles.logger.severe("unable to get user $username total XP")
            e.printStackTrace()
        }
        return 0
    }


    //TODO IMPLEMENT
    fun getUsersTames(player: Player):List<Pair<String,UUID>>{

        return emptyList()
    }


    //TODO IMPLEMENT
    fun getInventory(player: Player):List<Pair<Int,ItemStack>>{
        return emptyList()
    }


    //TODO IMPLEMENT
    fun getEnderChest(player:Player):List<Pair<Int, ItemStack>>{
        return emptyList()
    }

    //functions used for verification

    fun verifyUser(username:String,password:String):Boolean{


        val stmtString = """
            SELECT password,salt FROM users
            WHERE username = ?
        """.trimIndent()

        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString)
        stmt.setString(1,username)


        try {
            val rs = stmt.executeQuery()
            if(!rs.next()) return false
            val hashedPassword:ByteArray = rs.getBytes("password")
            val hashed = BCrypt.withDefaults().hash(6,rs.getBytes("salt"),password.toByteArray(StandardCharsets.UTF_8)) ?: return false;
            return hashed.contentEquals(hashedPassword);
        }catch (e:Exception){
            e.printStackTrace()
            return false

        }


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

    }

    //function used to update db

    fun updatePlayerInventory(player: Player){}

    fun updatePlayerEnderChest(player: Player){}

    fun createNewUser(player:Player,username: String,password: String):Boolean{
        val random: SecureRandom = SecureRandom()
        val bytes = ByteArray(16)
        random.nextBytes(bytes)
        val hashed = BCrypt.withDefaults().hash(6,bytes,password.toByteArray(StandardCharsets.UTF_8))?: return false
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
            (username,password,salt,lastLocX,lastLocY,lastLocZ,lastLocWorld,spawnLocX,spawnLocY,spawnLocZ,spawnWorld,noSpawnLocation,totalXP)
            VALUES
            (?,?,?,?,?,?,?,?,?,?,?,?,?)
        """.trimIndent()

        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(commandString);
        stmt.setString(1,username);
        stmt.setBytes(2,hashed)
        stmt.setBytes(3,bytes)
        stmt.setDouble(4, loc.x)
        stmt.setDouble(5, loc.y)
        stmt.setDouble(6, loc.z)
        stmt.setString(7,locWorld);
        stmt.setDouble(8,spawnLoc.x)
        stmt.setDouble(9,spawnLoc.y)
        stmt.setDouble(10,spawnLoc.z)
        stmt.setString(11,spawnWorld)
        stmt.setInt(12,spawnLocationExists)
        stmt.setInt(13,totalXP)
        try{
            stmt.executeUpdate()

        } catch (e:Exception){
            e.printStackTrace()
            return false
        }


        return true
    }

    fun updateExistingUser(player: Player):Boolean{
        val name:String = separateProfiles.playerNameMap[player] ?: return false
        val lastLoc = player.location.toVector();
        val lastWorld = player.world.name

        val spawnLocation = player.respawnLocation
        val spawnLocationExists = if(spawnLocation == null) 0 else 1
        val spawnLoc = spawnLocation?.toVector() ?: Vector(0,0,0)
        val spawnWorld = spawnLocation?.world?.name ?: "N/A"

        val totalXP = player.totalExperience

        val stmtString ="""
            UPDATE users
            SET 
            lastLocX = ?,lastLocY = ?,lastLocZ = ?,lastLocWorld = ?,
            spawnLocX = ?,spawnLocY = ?,spawnLocZ = ?,spawnWorld =?,noSpawnLocation = ?,
            totalXP = ?
            WHERE username = ?
        """.trimIndent()
        val stmt = separateProfiles.databaseHandler.sqlConnection.prepareStatement(stmtString)
        stmt.setDouble(1,lastLoc.x)
        stmt.setDouble(2,lastLoc.y)
        stmt.setDouble(3,lastLoc.z)
        stmt.setString(4,lastWorld)

        stmt.setDouble(5,spawnLoc.x)
        stmt.setDouble(6,spawnLoc.y)
        stmt.setDouble(7,spawnLoc.z)
        stmt.setString(8,spawnWorld)
        stmt.setInt(9,spawnLocationExists)

        stmt.setInt(10,totalXP)

        stmt.setString(11,name)
        try{
            stmt.executeUpdate()
        }catch (e:Exception){
            separateProfiles.logger.severe("Unable to update basic user info for player $name")
        }

        return true

    }

    fun addTame(tame: Tameable){}

    fun removeInvalidTames(player: Player){}

    //for now does not salt hashes
    fun createTables(){

        val userTable = """
            CREATE TABLE IF NOT EXISTS users(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                password BLOB NOT NULL,
                salt BLOB NOT NULL,
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