package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

public interface DataStore {

	/**
	 * Initialize storage
	 */
	void initialize() throws Exception;


	/**
	 * Check if datastore is initialized
	 * @return boolean - true if initialized, false if not
	 */
	boolean isInitialized();


	/**
	 * Get enum member representing datastore type
	 * @return DataStoreType
	 */
	DataStoreType getType();


	/**
	 * Get formatted display name for datastore type
	 * @return String formatted display name
	 */
	String getDisplayName();


	/**
	 * get all records from datastore
	 *
	 * @return List of all DeathRecords
	 */
	Collection<DeathRecord> selectAllRecords();


	/**
	 * Get record from datastore
	 *
	 * @param playerUUID the player UUID of the record to be retrieved
	 * @param worldUID   the world UID of the record to be retrieved
	 * @return death record or null if no matching record found
	 */
	DeathRecord selectRecord(final UUID playerUUID, final UUID worldUID);


	/**
	 * Insert a record in datastore
	 *
	 * @param deathRecord a DeathRecord to be inserted
	 */
	void insertRecord(final DeathRecord deathRecord);


	/**
	 * Insert records in datastore
	 *
	 * @param deathRecords a collection of DeathRecords to be inserted
	 * @return int number of records inserted
	 */
	int insertRecords(final Collection<DeathRecord> deathRecords);


	/**
	 * Delete record
	 *
	 * @param playerUUID the player uuid of the record to delete
	 * @param worldUID   the world uid of the record to delete
	 * @return the DeathRecord that was deleted from datastore
	 */
	@SuppressWarnings("unused")
	DeathRecord deleteRecord(final UUID playerUUID, final UUID worldUID);


	/**
	 * Close storage
	 */
	void close();


	/**
	 * Save datastore to disk, if applicable
	 */
	void save();


	/**
	 * Delete datastore file
	 */
	void delete();


	/**
	 * Check if datastore file exists
	 *
	 * @return {@code true} if file exists, {@code false} if file does not exist
	 */
	boolean exists();


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	static DataStore create(JavaPlugin plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(plugin, dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType new datastore type
	 * @param oldDataStore  existing datastore reference
	 * @return a new datastore instance of the given type
	 */
	static DataStore create(JavaPlugin plugin, final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			DataStoreType.convert(oldDataStore, newDataStore);
		}
		else {
			DataStoreType.convertAll(plugin, newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}


	/**
	 * Reload datastore, if configuration has changed
	 *
	 * @param plugin reference to plugin main class
	 */
	static void reload(PluginMain plugin) {

		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = create(plugin, newType, plugin.dataStore);
		}
	}

}
