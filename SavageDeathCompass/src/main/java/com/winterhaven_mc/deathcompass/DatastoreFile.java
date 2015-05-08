package com.winterhaven_mc.deathcompass;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class DatastoreFile extends Datastore {
	
	DeathCompassMain plugin = DeathCompassMain.instance;
	private ConfigAccessor deathlocationsfile;
	private int fileSyncTaskID;

	/**
	 * Initialize flat file Datastore object and
	 * create repeating task to sync file to disk
	 */
	@Override
	void initialize() throws Exception {
		deathlocationsfile = new ConfigAccessor(plugin, "deathlocations.yml");
		deathlocationsfile.saveDefaultConfig();
		
		plugin.getLogger().info("Flat file storage initialized.");
		
		// get file-sync-interval in minutes from config and convert to ticks
		Long fileSyncTicks = (long) (plugin.getConfig().getInt("file-sync-interval",5) * 60 * 20);
		
		// create repeating task to sync deathlocationsfile to disk
		fileSyncTaskID = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new BukkitRunnable() {
			public void run() {
				deathlocationsfile.saveConfig();
			}
		}, 1200, fileSyncTicks);
		
		if (plugin.debug) {
			plugin.getLogger().info("File sync task created with task id: " + fileSyncTaskID);
		}
		convertFromSQLite();
	}

	/**
	 * Retrieve player death location from flat file Datastore
	 * @param player
	 * @return location
	 * @throws exception
	 */
	@Override
	Location getRecord(Player player) {
		
		//set playerid to player name
		String playerid = player.getName();
		
		// if use-uuid is enabled in config, set playerid to player uuid
		if (plugin.getConfig().getBoolean("use-uuid", true)) {
			playerid = player.getUniqueId().toString();
		}
		
		// set worldname to player current world
		String worldname = player.getWorld().getName();

		// set lastdeathloc to player bed spawn location
		Location lastdeathloc = player.getBedSpawnLocation();
		
		// if player bedspawn is null, set lastdeathloc to world spawn location
		if (lastdeathloc == null) {
			lastdeathloc = player.getWorld().getSpawnLocation();
		}
		
		// if player last death location in current world is in data file, set it in lastdeathloc variable 
		if (deathlocationsfile.getConfig().contains(playerid + "." + worldname)) {
			lastdeathloc.setWorld(player.getWorld());
			lastdeathloc.setX(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".x"));
			lastdeathloc.setY(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".y"));
			lastdeathloc.setZ(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".z"));
			if (plugin.debug) {
				plugin.getLogger().info("Player record retrieved. Location: " + lastdeathloc.toString());
			}
		}
		// return location
		return lastdeathloc;
	}

	/**
	 * Write player current (death) location to file object
	 * @param player
	 * @throws exception
	 */
	@Override
	void putRecord(Player player) {
		
		// set playerid to player uuid
		String playerID = player.getUniqueId().toString();
		
		// if use-uuid is not enabled in config, set playerid to player name
		if (!plugin.getConfig().getBoolean("use-uuid", true)) {
			playerID = player.getName();
		}

		// get player current location
		Location location = player.getLocation();
		
		// get player location world name
		String worldname = location.getWorld().getName();
		
		// output debugging info
		if (plugin.debug) {
			plugin.getLogger().info("Inserting record for " + player.getName() + ": " + location.toString());
		}

		// save location in data file
		deathlocationsfile.getConfig().set(playerID + "." + worldname + ".x", location.getBlockX());
		deathlocationsfile.getConfig().set(playerID + "." + worldname + ".y", location.getBlockY());
		deathlocationsfile.getConfig().set(playerID + "." + worldname + ".z", location.getBlockZ());
		deathlocationsfile.getConfig().set(playerID + "." + worldname + ".timestamp", System.currentTimeMillis());
	}

	/**
	 * Close flat file
	 * @throws exception
	 */
	@Override
	void close() {
		
		// sync to disk
		deathlocationsfile.saveConfig();
		
		// cancel fileSyncTask
		plugin.getServer().getScheduler().cancelTask(fileSyncTaskID);
		if (plugin.debug) {
			plugin.getLogger().info("Cancelled File Sync Task ID: " + fileSyncTaskID);
		}
	}
	
	/**
	 * Get time of players death stored in flat file
	 * @param player
	 * @param worldName
	 * @return timestamp
	 */
	long getTimestamp(Player player, String worldName) {
		
		String playerID = player.getName();
		
		Long timestamp = deathlocationsfile.getConfig().getLong(playerID + "." + worldName + ".timestamp");
		if (timestamp == null) {
			timestamp = (long) 0;
		}
		return timestamp;
	}

	/**
	 * Convert Datastore to flat file from SQLite
	 * @throws SQLException
	 */
	private void convertFromSQLite() throws SQLException {
		
		// get handle to deathlocations.db file
		File dbFile = new File(plugin.getDataFolder(), "deathlocations.db");
		
		// if deathlocations.db file exists, convert records to sqlite
		if (dbFile.exists()) {
			
			// register the driver 
			String jdbcDriverName = "org.sqlite.JDBC";
			
			try {
				Class.forName(jdbcDriverName);
			}
			catch (Exception e) {
				plugin.getLogger().severe(e.getMessage());
				return;
			}
			
			// create database url
			String deathLocationsDb = plugin.getDataFolder() + File.separator + "deathlocations.db";
			String jdbc = "jdbc:sqlite";
			String dbUrl = jdbc + ":" + deathLocationsDb;

			// create a database connection
			Connection connection = null;
			try {
				connection = DriverManager.getConnection(dbUrl);
			} catch (SQLException e) {
				plugin.getLogger().warning("Could not create connection to SQLite database.");
			}

			String playerID;
			String worldName;
			int x,y,z;
			long timestamp;
			int rowcount = 0;
			
			String sqlSelect = "SELECT * FROM deathlocations";

			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect);

			ResultSet rs = preparedStatement.executeQuery();
			while( rs.next() ) {
				playerID = rs.getString("playerid");
				worldName = rs.getString("worldname");
				x = rs.getInt("x");
				y = rs.getInt("y");
				z = rs.getInt("z");
				timestamp = rs.getLong("timestamp");
				rowcount++;
				
				// save location in data file
				deathlocationsfile.getConfig().set(playerID + "." + worldName + ".x", x);
				deathlocationsfile.getConfig().set(playerID + "." + worldName + ".y", y);
				deathlocationsfile.getConfig().set(playerID + "." + worldName + ".z", z);
				deathlocationsfile.getConfig().set(playerID + "." + worldName + ".timestamp", timestamp);			
			}
			// close database connection
			connection.close();
			
			// sync flat file Datastore to disk
			deathlocationsfile.saveConfig();
			
			// output number of records converted to log
			plugin.getLogger().info(rowcount + " records inserted in flat file storage.");

			// delete database file
			dbFile.delete();
		
			// relase database file handle
			dbFile = null;
		}
	}

}
