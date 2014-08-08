package com.winterhaven_mc.deathcompass;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * SQLite implementation of Datastore
 * for saving player death locations
 * @author Tim Savage
 *
 */
public class DatastoreSQLite extends Datastore {
	
	// static reference to main class
	private DeathCompassMain plugin = DeathCompassMain.plugin;

	// database connection object
	private Connection connection;


	/**
	 * initialize the database connection and
	 * create table if one doesn't already exist
	 */
	@Override
	public void initializeDb() throws SQLException {

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

		// sql statement to create table if it doesn't already exist
		String makePlayerTable = "CREATE TABLE IF NOT EXISTS deathlocations (" +
				"playerid VARCHAR(36) NOT NULL," +
				"worldname VARCHAR(255) NOT NULL," +
				"x INTEGER, " +
				"y INTEGER, " +
				"z INTEGER, " +
				"timestamp INTEGER, " +
				"UNIQUE (playerid,worldname) )";

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(makePlayerTable);
		
		// output status to log
		plugin.getLogger().info("SQLite database intialized.");
		
		// convert records from flat file if necessary
		convertFromFile("deathlocations.yml");
	}


	/**
	 * Retrive player death location from SQLite database
	 * @param player
	 * @return location
	 */
	@Override
	Location getRecord(Player player) throws Exception {
		
		World world = player.getWorld();
		String worldName = world.getName();
		String playerID = player.getUniqueId().toString();

		// if config setting use-uuid is false, use player name for playerID
		if (!plugin.getConfig().getBoolean("use-uuid",true)) {
			playerID = player.getName();
		}

		// sql select statement to retrieve rows from database
		String sql = "SELECT * FROM deathlocations WHERE playerid = ? AND worldname = ?";

		// create sql prepared statement
		PreparedStatement preparedStatement = connection.prepareStatement(sql);

		preparedStatement.setString(1, playerID);
		preparedStatement.setString(2, worldName);

		// execute sql query
		ResultSet rs = preparedStatement.executeQuery();

		// initialize location variables
		Integer x = 0;
		Integer y = 0;
		Integer z = 0;
		
		boolean empty = true;

		// iterate through returned rows and set location variables (there should be only 1 row returned)
		while( rs.next() ) {
			x = rs.getInt("x");
			y = rs.getInt("y");
			z = rs.getInt("z");
		    empty = false;
		}

		// if no matching location found for player in world, write log message and return null
		if (empty) {
			plugin.getLogger().info("No death location found for " + player.getName() + " in world " + worldName + ".");
			return null;
		}
		
		// create new location with results and return location
		Location location = new Location(world, x.doubleValue() , y.doubleValue(), z.doubleValue());
		return location;
	}
	

	/**
	 * Insert player death location into SQLite database
	 * @param player
	 */
	@Override
	void putRecord(Player player) throws Exception {

		// set playerID to player UUID
		String playerID = player.getUniqueId().toString();

		// set location to players current (death) location
		Location location = player.getLocation();
		
		// if config setting use-uuid is false, use player name for playerID
		if (!plugin.getConfig().getBoolean("use-uuid",true)) {
			playerID = player.getName();
		}
		
		// get worldname as string, x,y,z as int from location
		String worldName = location.getWorld().getName();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		long timestamp = System.currentTimeMillis();

		// sql statement to insert or replace record
		String sql = "INSERT OR REPLACE INTO deathlocations (playerid,worldname,x,y,z,timestamp) " +
				"values(?,?,?,?,?,?) ";

		// create prepared statement
		PreparedStatement preparedStatement = connection.prepareStatement(sql);

		preparedStatement.setString(1, playerID);
		preparedStatement.setString(2, worldName);
		preparedStatement.setInt(3, x);
		preparedStatement.setInt(4, y);
		preparedStatement.setInt(5, z);
		preparedStatement.setLong(6, timestamp);
		
		// execute prepared statement
		int rowsAffected = preparedStatement.executeUpdate();
		
		// output debugging information
		if (plugin.debug) {
			plugin.getLogger().info(rowsAffected + " rows affected.");
		}
	}

	
	/**
	 * Close database connection
	 */
	@Override
	void closeDb() throws Exception {

		connection.close();
		plugin.getLogger().info("SQLite database connection closed.");		
	}

	/**
	 * convert Datastore from flat file to SQLite database
	 * @param filename
	 * @throws SQLException
	 */
	void convertFromFile(String filename) throws SQLException {
		
		int totalRowsAffected = 0;
		
		// get handle to deathlocations.yml file
		File flatFile = new File(plugin.getDataFolder(), filename);
		
		// if deathlocations.yml file exists, convert records to sqlite
		if (flatFile.exists()) {
			ConfigAccessor ff = new ConfigAccessor(plugin,"deathlocations.yml");
			for (String playerID : ff.getConfig().getKeys(false)) {
				Set<String> worldNames = ff.getConfig().getConfigurationSection(playerID).getKeys(false);
				for (String worldName : worldNames) {
					int x = ff.getConfig().getInt(playerID + "." + worldName + ".x");
					int y = ff.getConfig().getInt(playerID + "." + worldName + ".y");
					int z = ff.getConfig().getInt(playerID + "." + worldName + ".z");
					Long timestamp = ff.getConfig().getLong(playerID + "." + worldName + ".timestamp");
					
					// if flat file doesn't have timestamp field, use timestamp = 0 (to convert old versions)
					if (timestamp == null) {
						timestamp = (long)0;
					}

					String sqlSelect = "SELECT * FROM deathlocations WHERE playerid = ? AND worldname = ?";
					
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(sqlSelect);

					preparedStatement.setString(1, playerID);
					preparedStatement.setString(2, worldName);
					
					ResultSet rs = preparedStatement.executeQuery();

					Long dbtimestamp = (long)0;
					
					boolean empty = true;

					while( rs.next() ) {
						dbtimestamp = rs.getLong("timestamp");
					    empty = false;
					}

					// if result set is empty or flat file timestamp is newer than dbtimestamp, insert/update record
					if (empty || timestamp > dbtimestamp) {
						
						// sql statement to insert or replace record
						String sql = "INSERT OR REPLACE INTO deathlocations (playerid,worldname,x,y,z,timestamp) " +
								"values(?,?,?,?,?,?) ";
						
						// create prepared statement
						preparedStatement = connection.prepareStatement(sql);

						preparedStatement.setString(1, playerID);
						preparedStatement.setString(2, worldName);
						preparedStatement.setInt(3, x);
						preparedStatement.setInt(4, y);
						preparedStatement.setInt(5, z);
						preparedStatement.setLong(6, timestamp);
						
						// execute prepared statement
						int rowsAffected = preparedStatement.executeUpdate();
						ff.getConfig().set(playerID + "." + worldName, null);
						totalRowsAffected += rowsAffected;
					}		
				}
			}
			plugin.getLogger().info(totalRowsAffected + " locations transfered from flat file to SQLite database.");
			
			// release DatastoreFile object
			ff = null;
			
			// delete file
			flatFile.delete();

			// release file handle object
			flatFile = null;
		}
	}
	
}
