package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


class DataStoreSQLite extends DataStore implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;

	// death record cache
	private final DeathRecordCache deathRecordCache;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	DataStoreSQLite(final PluginMain plugin) {

		// reference to main class
		this.plugin = plugin;

		// set datastore type
		this.type = DataStoreType.SQLITE;

		// set filename
		this.filename = "deathlocations.db";

		// initialize death record cache
		deathRecordCache = new DeathRecordCache();
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

		// register the driver
		final String jdbcDriverName = "org.sqlite.JDBC";

		Class.forName(jdbcDriverName);

		// create database url
		String dbFile = plugin.getDataFolder() + File.separator + filename;
		String jdbc = "jdbc:sqlite";
		String dbUrl = jdbc + ":" + dbFile;

		// create a database connection
		connection = DriverManager.getConnection(dbUrl);
		Statement statement = connection.createStatement();

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("createDeathLocationTable"));

		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info("sqlite datastore initialized.");
		}

	}


	@Override
	public DeathRecord getRecord(final UUID playerUUID, final String worldName) {

		// if key is null return null record
		if (playerUUID == null) {
			return null;
		}

		// try to get world uid from world name
		//noinspection ConstantConditions
		UUID worldUID = plugin.getServer().getWorld(worldName).getUID();

		// if world uid is null, return null record
		//noinspection ConstantConditions
		if (worldUID == null) {
			return null;
		}

		// try cache first
		DeathRecord deathRecord = deathRecordCache.get(playerUUID, worldUID);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (deathRecord != null) {
			return deathRecord;
		}

		// convert playerUUID to string
		String playerUUIDString = playerUUID.toString();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectLocation"));

			preparedStatement.setString(1, playerUUIDString);
			preparedStatement.setString(2, worldName);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next()) {

				// get stored world and coordinates
				preparedStatement.setString(1, playerUUIDString);
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");

				if (plugin.getServer().getWorld(worldName) == null) {
					plugin.getLogger().warning("Stored world not found!");
					return null;
				}

				World world = plugin.getServer().getWorld(worldName);
				Location location = new Location(world, x, y, z);
				deathRecord = new DeathRecord(playerUUID, location);
			}
		}
		catch (SQLException e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while fetching a record from the SQLite database.");
			plugin.getLogger().warning(e.getLocalizedMessage());

			// if debugging is enabled, output stack trace
			if (plugin.debug) {
				e.getStackTrace();
			}
			return null;
		}

		// if record is not null, put record in cache
		if (deathRecord != null) {
			deathRecordCache.put(deathRecord);
		}

		// return record
		return deathRecord;
	}


	@Override
	public void putRecord(final DeathRecord deathRecord) {

		// if record is null do nothing and return
		if (deathRecord == null) {
			return;
		}

		// cache death record
		deathRecordCache.put(deathRecord);

		// get playerUUID as string
		final String playerUUIDString = deathRecord.getPlayerUUID().toString();

		// get location
		final Location location = deathRecord.getLocation();

		// get world name
		String testWorldName;

		// test that world in destination location is valid
		try {
			//noinspection ConstantConditions
			testWorldName = location.getWorld().getName();
		}
		catch (Exception e) {
			plugin.getLogger().warning("An error occurred while inserting"
					+ " a record in the SQLite database. World invalid!");
			return;
		}
		final String worldName = testWorldName;

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertLocation"));

					preparedStatement.setString(1, playerUUIDString);
					preparedStatement.setString(2, worldName);
					preparedStatement.setDouble(3, location.getX());
					preparedStatement.setDouble(4, location.getY());
					preparedStatement.setDouble(5, location.getZ());

					// execute prepared statement
					preparedStatement.executeUpdate();
				}
				catch (Exception e) {

					// output simple error message
					plugin.getLogger().warning("An error occurred while inserting a record into the SQLite database.");
					plugin.getLogger().warning(e.getLocalizedMessage());

					// if debugging is enabled, output stack trace
					if (plugin.debug) {
						e.getStackTrace();
					}
				}
			}
		}.runTaskAsynchronously(plugin);
	}


	@Override
	List<DeathRecord> getAllRecords() {

		List<DeathRecord> returnList = new ArrayList<>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllLocations"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				String key = rs.getString("playerid");
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");

				World world;

				try {
					world = plugin.getServer().getWorld(worldName);
				}
				catch (Exception e) {
					plugin.getLogger().warning("Stored record has invalid world: "
							+ worldName + ". Skipping record.");
					continue;
				}

				// convert key string to UUID
				UUID playerUUID = null;
				try {
					playerUUID = UUID.fromString(key);
				}
				catch (Exception e) {
					if (plugin.debug) {
						plugin.getLogger().warning("Player UUID in datastore is invalid!");
					}
				}

				// if playerUUID is null, do not add record to return list
				if (playerUUID != null) {
					Location location = new Location(world, x, y, z);
					DeathRecord deathRecord = new DeathRecord(playerUUID, location);
					returnList.add(deathRecord);
				}
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
	DeathRecord deleteRecord(final UUID playerUUID, final String worldName) {

		// if key is null return null record
		if (playerUUID == null || worldName == null || worldName.isEmpty()) {
			return null;
		}

		// get destination record to be deleted, for return
		DeathRecord deathRecord = getRecord(playerUUID, worldName);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteLocation"));

			preparedStatement.setString(1, playerUUID.toString());
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
	public void close() {

		try {
			connection.close();
			plugin.getLogger().info("SQLite database connection closed.");
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while closing the SQLite database connection.");
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
	boolean delete() {

		boolean result = false;
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			result = dataStoreFile.delete();
		}
		return result;
	}


	@Override
	boolean exists() {

		// get path name to old data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();

	}

}
