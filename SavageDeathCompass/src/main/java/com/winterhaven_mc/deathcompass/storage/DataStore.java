package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

public interface DataStore {

	/**
	 * Initialize storage
	 */
	void initialize() throws Exception;


	boolean isInitialized();


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
	 * get all records from datastore
	 *
	 * @return List of all DeathRecords
	 */
	Collection<DeathRecord> selectAllRecords();


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

	DataStoreType getType();

	String getDisplayName();

	/**
	 * Save datastore to disk
	 */
	void save();


	/**
	 * Delete datastore file
	 *
	 * @return boolean {@code true} if deletion was successful, {@code false} if unsuccessful
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


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
	static DataStore create(PluginMain plugin) {

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
	static DataStore create(PluginMain plugin, final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.toString() + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
		}

		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convert(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}


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


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	static void convert(final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			Bukkit.getLogger().info("Converting existing " + oldDataStore.getDisplayName() + " datastore to "
					+ newDataStore.getDisplayName() + " datastore...");

			// initialize old datastore
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					Bukkit.getLogger().warning("Could not initialize "
							+ oldDataStore + " datastore for conversion.");
					Bukkit.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			Collection<DeathRecord> allRecords = oldDataStore.selectAllRecords();

			int count = newDataStore.insertRecords(allRecords) ;

			Bukkit.getLogger().info(count + " records converted to new datastore.");

			newDataStore.save();

			oldDataStore.close();
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the datastore to convert all other existing datastore to
	 */
	static void convertAll(final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStoresTypes = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStoresTypes.remove(newDataStore.getType());

		for (DataStoreType type : dataStoresTypes) {

			// create oldDataStore holder
			DataStore oldDataStore = type.create();

			if (oldDataStore != null && oldDataStore.exists()) {

				convert(oldDataStore, newDataStore);
			}
		}
	}

}
