package com.winterhaven_mc.deathcompass;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;


public class DataStoreSQLite extends DataStore {

	// reference to main class
	private final DeathCompassMain plugin;
	
	// database connection object
	private Connection connection;
	
	// data store file name
	private static final String FILENAME = "deathlocations.db";

	// data store type
	private static final DataStoreType TYPE = DataStoreType.SQLITE;
	
	DataStoreSQLite (DeathCompassMain plugin) {

		// reference to main class
		this.plugin = plugin;	
	}
	
	
	@Override
	void initialize() throws SQLException, ClassNotFoundException {
		
		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			if (plugin.debug) {
				plugin.getLogger().info("sqlite datastore already initialized.");
			}
			return;
		}

		// sql statement to create table if it doesn't already exist
		String createDeathLocationTable = "CREATE TABLE IF NOT EXISTS deathlocations (" +
				"playerid VARCHAR(36) NOT NULL," +
				"worldname VARCHAR(255) NOT NULL," +
				"x INTEGER, " +
				"y INTEGER, " +
				"z INTEGER, " +
				"UNIQUE (playerid,worldname) )";

		// register the driver 
		final String jdbcDriverName = "org.sqlite.JDBC";
		
		Class.forName(jdbcDriverName);

		// create database url
		String dbFile = plugin.getDataFolder() + File.separator + FILENAME;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + dbFile;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(createDeathLocationTable);
		
		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info("sqlite datastore initialized.");
		}

	}
	
	@Override
	DeathRecord getRecord(String playerId, String worldName) {
		
		// if key is null return null record
		if (playerId == null) {
			return null;
		}
		
		DeathRecord deathRecord = null;
		World world = null;
		
		final String sqlGetDestination = "SELECT * FROM deathlocations WHERE playerid = ? AND worldname = ?";

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlGetDestination);
			
			preparedStatement.setString(1, playerId);
			preparedStatement.setString(2, worldName);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next()) {
			
				// get stored world and coordinates
				preparedStatement.setString(1, playerId);
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				
				if (plugin.getServer().getWorld(worldName) == null) {
					plugin.getLogger().warning("Stored world not found!");
					return null;
				}
				world = plugin.getServer().getWorld(worldName);
				Location location = new Location(world,x,y,z);
				deathRecord = new DeathRecord(playerId,location);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while fetching a record from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			
			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
			return null;
		}
		return deathRecord;
	}
	
	@Override
	void putRecord(DeathRecord deathRecord) {
		
		// if record is null do nothing and return
		if (deathRecord == null) {
			return;
		}
		
		// get key
		final String playerId = deathRecord.getPlayerId();
		
		// get location
		final Location location = deathRecord.getLocation();
		
		// get world name
		String testWorldName = null;

		// test that world in destination location is valid
		try {
			testWorldName = location.getWorld().getName();
		} catch (Exception e) {
			plugin.getLogger().warning("An error occured while inserting"
					+ " a record in the SQLite database. World invalid!");
			return;
		}
		final String worldName = testWorldName;
		
		// sql statement to insert or replace record
		final String sqlInsertRecord = "INSERT OR REPLACE INTO deathlocations ("
				+ "playerid, "
				+ "worldname, "
				+ "x, "
				+ "y, "
				+ "z) "
				+ "values(?,?,?,?,?)";

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(sqlInsertRecord);

					preparedStatement.setString(1, playerId);
					preparedStatement.setString(2, worldName);
					preparedStatement.setDouble(3, location.getX());
					preparedStatement.setDouble(4, location.getY());
					preparedStatement.setDouble(5, location.getZ());

					// execute prepared statement
					preparedStatement.executeUpdate();
				}
				catch (Exception e) {

					// output simple error message
					plugin.getLogger().warning("An error occured while inserting a record into the SQLite database.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}		
			}
		}.runTaskAsynchronously(plugin);
	}
	
	List<DeathRecord> getAllRecords() {
		
		List<DeathRecord> returnList = new ArrayList<DeathRecord>();

		// sql statement to retrieve all display names
		final String sqlSelectAllRecords = "SELECT * FROM deathlocations";
		
		try {
			PreparedStatement preparedStatement = connection.prepareStatement(sqlSelectAllRecords);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				String key = rs.getString("playerid");
				String worldName = rs.getString("worldname");
				Double x = rs.getDouble("x");
				Double y = rs.getDouble("y");
				Double z = rs.getDouble("z");
				
				World world;
				
				try {
					world = plugin.getServer().getWorld(worldName);
				} catch (Exception e) {
					plugin.getLogger().warning("Stored record has invalid world: " 
							+ worldName + ". Skipping record.");
					continue;
				}
				
				Location location = new Location(world,x,y,z);
				DeathRecord deathRecord = new DeathRecord(key,location);
				returnList.add(deathRecord);
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying to fetch all records from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}

		// return results
		return returnList;
		
	}
	
	@Override
	DeathRecord deleteRecord(String playerId, String worldName) {
		
		// if key is null return null record
		if (playerId == null || playerId.isEmpty() || worldName == null || worldName.isEmpty()) {
			return null;
		}

		// get destination record to be deleted, for return
		DeathRecord deathRecord = getRecord(playerId,worldName);

		final String sqlDeleteDestination = "DELETE FROM deathlocations "
				+ "WHERE playerid = ? AND worldname = ?";
		
		try {
			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(sqlDeleteDestination);

			preparedStatement.setString(1, playerId);
			preparedStatement.setString(1, worldName);

			// execute prepared statement
			int rowsAffected = preparedStatement.executeUpdate();
			
			// output debugging information
			if (plugin.debug) {
				plugin.getLogger().info(rowsAffected + " rows deleted.");
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting to delete a record from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}		
		return deathRecord;
	}

	@Override
	void close() {

		try {
			connection.close();
			plugin.getLogger().info("SQLite database connection closed.");		
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occured while closing the SQLite database connection.");
			plugin.getLogger().warning(e.getMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
		}
		setInitialized(false);
	}
	
	@Override
	void save() {
	
		// no action necessary for this storage type
		
	}
	
	@Override
	void delete() {
		
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}
	
	@Override
	boolean exists() {
		
		// get path name to old data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();

	}
	
	@Override
	String getFilename() {
		return FILENAME;
	}
	
	@Override
	DataStoreType getType() {
		return TYPE;
	}

}
