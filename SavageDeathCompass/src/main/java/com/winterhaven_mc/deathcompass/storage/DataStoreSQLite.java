package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;


class DataStoreSQLite extends DataStore implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// database connection object
	private Connection connection;

	// death record cache
	private final DeathRecordCache deathRecordCache;

	// schema version
	private int schemaVersion;


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
				plugin.getLogger().info("SQLite datastore already initialized.");
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

		// update database schema if necessary
		updateSchema();

		// set initialized true
		setInitialized(true);
		plugin.getLogger().info("SQLite datastore initialized.");
	}


	private int getSchemaVersion() {

		int version = -1;

		try {
			final Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(Queries.getQuery("GetUserVersion"));

			while (rs.next()) {
				version = rs.getInt(1);
			}
		}
		catch (SQLException e) {
			plugin.getLogger().warning("Could not get schema version!");
		}
		return version;
	}


	private void updateSchema() throws SQLException {

		schemaVersion = getSchemaVersion();

		final Statement statement = connection.createStatement();

		if (schemaVersion == 0) {
			int count;

			// check for existing table
			ResultSet rs = statement.executeQuery(Queries.getQuery("SelectDeathLocationTable"));
			if (rs.next()) {

				// get existing records
				Collection<DeathRecord> existingRecords = selectAllRecords();

				// drop schema v0 table
				statement.executeUpdate(Queries.getQuery("DropDeathLocationTable"));

				// create schema v1 table
				statement.executeUpdate(Queries.getQuery("CreateDeathLocationTable"));

				// insert existing records in new table
				count = insertRecords(existingRecords);
				plugin.getLogger().info(count + " records migrated to table schema v1.");
			}

			// update schema version in database
			statement.executeUpdate("PRAGMA user_version = 1");

			// update schema version field
			schemaVersion = 1;
		}

		// execute table creation statement
		statement.executeUpdate(Queries.getQuery("CreateDeathLocationTable"));
	}


	@Override
	public synchronized DeathRecord selectRecord(final UUID playerUUID, final UUID worldUID) {

		// if key is null return null record
		if (playerUUID == null) {
			return null;
		}

		// if world uid is null, return null record
		if (worldUID == null) {
			return null;
		}

		// get player uuid components
		final long playerUidMsb = playerUUID.getMostSignificantBits();
		final long playerUidLsb = playerUUID.getLeastSignificantBits();

		// get world uid components
		final long worldUidMsb = worldUID.getMostSignificantBits();
		final long worldUidLsb = worldUID.getLeastSignificantBits();

		// try cache first
		DeathRecord deathRecord = deathRecordCache.get(playerUUID, worldUID);

		// if a record was returned from cache, return the record; otherwise try datastore
		if (deathRecord != null) {
			return deathRecord;
		}

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectLocation"));

			preparedStatement.setLong(1, playerUidMsb);
			preparedStatement.setLong(2, playerUidLsb);
			preparedStatement.setLong(3, worldUidMsb);
			preparedStatement.setLong(4, worldUidLsb);

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			// only zero or one record can match the unique key
			if (rs.next()) {

				// get stored world and coordinates
				String worldName = rs.getString("worldname");
				double x = rs.getDouble("x");
				double y = rs.getDouble("y");
				double z = rs.getDouble("z");

				// get server world by uid
				World world = plugin.getServer().getWorld(worldUID);

				if (world == null) {
					plugin.getLogger().warning("World " + worldName + " is not loaded!");
					return null;
				}

				deathRecord = new DeathRecord(playerUUID, worldUID, x, y, z);
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
	synchronized Collection<DeathRecord> selectAllRecords() {

		Collection<DeathRecord> returnList = new ArrayList<>();

		try {
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("SelectAllLocations"));

			// execute sql query
			ResultSet rs = preparedStatement.executeQuery();

			while (rs.next()) {

				if (schemaVersion == 0) {
					String key = rs.getString("playerid");
					String worldName = rs.getString("worldname");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");

					World world = plugin.getServer().getWorld(worldName);

					if (world == null) {
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

					// if playerUUID is not null, add record to return list
					if (playerUUID != null) {
						DeathRecord deathRecord = new DeathRecord(playerUUID, world.getUID(), x, y, z);
						returnList.add(deathRecord);
					}
				}

				// if schema version 1, try to get world by uuid
				else if (schemaVersion == 1) {

					long playerUidMsb = rs.getLong("playerUidMsb");
					long playerUidLsb = rs.getLong("playerUidLsb");
					String worldName = rs.getString("worldname");
					long worldUidMsb = rs.getLong("worldUidMsb");
					long worldUidLsb = rs.getLong("worldUidLsb");
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					double z = rs.getDouble("z");

					World world = plugin.getServer().getWorld(new UUID(worldUidMsb, worldUidLsb));

					if (world == null) {
						plugin.getLogger().warning("Stored record has invalid world: "
								+ worldName + ". Skipping record.");
						continue;
					}

					// convert components to player uuid
					UUID playerUUID = new UUID(playerUidMsb, playerUidLsb);

					DeathRecord deathRecord = new DeathRecord(playerUUID, world.getUID(), x, y, z);
					returnList.add(deathRecord);
				}
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while trying "
					+ "to fetch all records from the SQLite database.");
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
	public synchronized void insertRecord(final DeathRecord deathRecord) {

		// if record is null do nothing and return
		if (deathRecord == null) {
			return;
		}

		// cache death record
		deathRecordCache.put(deathRecord);

		// get player uid components
		final long playerUidMsb = deathRecord.getPlayerUid().getMostSignificantBits();
		final long playerUidLsb = deathRecord.getPlayerUid().getLeastSignificantBits();

		// get world
		final World world = plugin.getServer().getWorld(deathRecord.getWorldUid());

		// if world is null, log message and return
		if (world == null) {
			plugin.getLogger().warning("An error occurred while inserting"
					+ " a record in the SQLite database. World invalid!");
			return;
		}

		// get world name
		final String worldName = world.getName();

		// get world uid components
		final long worldUidMsb = world.getUID().getMostSignificantBits();
		final long worldUidLsb = world.getUID().getLeastSignificantBits();

		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					// create prepared statement
					PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertLocation"));

					preparedStatement.setLong(1, playerUidMsb);
					preparedStatement.setLong(2, playerUidLsb);
					preparedStatement.setString(3, worldName);
					preparedStatement.setLong(4, worldUidMsb);
					preparedStatement.setLong(5, worldUidLsb);
					preparedStatement.setDouble(6, deathRecord.getX());
					preparedStatement.setDouble(7, deathRecord.getY());
					preparedStatement.setDouble(8, deathRecord.getZ());

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
	public synchronized int insertRecords(final Collection<DeathRecord> deathRecords) {

		// if record is null do nothing and return
		if (deathRecords == null) {
			return 0;
		}

		int count = 0;

		for (DeathRecord deathRecord : deathRecords) {

			// cache death record
			deathRecordCache.put(deathRecord);

			// get player uid components
			final long playerUidMsb = deathRecord.getPlayerUid().getMostSignificantBits();
			final long playerUidLsb = deathRecord.getPlayerUid().getLeastSignificantBits();

			// get world
			final World world = plugin.getServer().getWorld(deathRecord.getWorldUid());

			// if world is null, skip to next record
			if (world == null) {
				plugin.getLogger().warning("An error occurred while inserting"
						+ " a record in the SQLite database. World invalid!");
				continue;
			}

			// get world name
			final String worldName = world.getName();

			// get world uid components
			final long worldUidMsb = world.getUID().getMostSignificantBits();
			final long worldUidLsb = world.getUID().getLeastSignificantBits();

			new BukkitRunnable() {
				@Override
				public void run() {
					try {
						// create prepared statement
						PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("InsertLocation"));

						preparedStatement.setLong(1, playerUidMsb);
						preparedStatement.setLong(2, playerUidLsb);
						preparedStatement.setString(3, worldName);
						preparedStatement.setLong(4, worldUidMsb);
						preparedStatement.setLong(5, worldUidLsb);
						preparedStatement.setDouble(6, deathRecord.getX());
						preparedStatement.setDouble(7, deathRecord.getY());
						preparedStatement.setDouble(8, deathRecord.getZ());


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
			count++;
		}
		return count;
	}


	@Override
	synchronized DeathRecord deleteRecord(final UUID playerUid, final UUID worldUid) {

		// if player uuid is null return null record
		if (playerUid == null) {
			return null;
		}

		// if world uid is null return null record
		if (worldUid == null) {
			return null;
		}

		// get player uid components
		final long playerUidMsb = playerUid.getMostSignificantBits();
		final long playerUidLsb = playerUid.getLeastSignificantBits();

		// get world uid components
		final long worldUidMsb = worldUid.getMostSignificantBits();
		final long worldUidLsb = worldUid.getLeastSignificantBits();

		// get destination record to be deleted, for return
		DeathRecord deathRecord = selectRecord(playerUid, worldUid);

		try {
			// create prepared statement
			PreparedStatement preparedStatement = connection.prepareStatement(Queries.getQuery("DeleteLocation"));

			preparedStatement.setLong(1, playerUidMsb);
			preparedStatement.setLong(2, playerUidLsb);
			preparedStatement.setLong(3, worldUidMsb);
			preparedStatement.setLong(4, worldUidLsb);

			// execute prepared statement
			int rowsAffected = preparedStatement.executeUpdate();

			// output debugging information
			if (plugin.debug) {
				plugin.getLogger().info(rowsAffected + " rows deleted.");
			}
		}
		catch (Exception e) {

			// output simple error message
			plugin.getLogger().warning("An error occurred while attempting "
					+ "to delete a record from the SQLite database.");
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
