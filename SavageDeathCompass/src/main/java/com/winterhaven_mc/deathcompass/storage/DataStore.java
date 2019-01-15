package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


/**
 * Abstract class declares methods that need to be implemented for plugin data stores.
 */
public abstract class DataStore {

	// static reference to main class
	private final static PluginMain plugin = PluginMain.INSTANCE;

	// datastore initialized state
	private boolean initialized;

	// datastore type
	DataStoreType type;

	// datastore filename
	String filename;


	/**
	 * Initialize storage
	 */
	abstract void initialize() throws Exception;


	/**
	 * Get record from datastore
	 *
	 * @param playerUUID the player UUID for the record to be retrieved
	 * @param worldName  the world name for the record to be retrieved
	 * @return death record or null if no matching record found
	 */
	public abstract DeathRecord getRecord(final UUID playerUUID, final String worldName);


	/**
	 * Store record in datastore
	 *
	 * @param deathRecord the DeathRecord to be stored
	 */
	public abstract void putRecord(final DeathRecord deathRecord);


	/**
	 * get all records from datastore
	 *
	 * @return List of all DeathRecords
	 */
	abstract List<DeathRecord> getAllRecords();


	/**
	 * Delete record
	 *
	 * @param playerUUID the player uuid of the record to delete
	 * @param worldName  the world name of the record to delete
	 * @return the DeathRecord that was deleted from datastore
	 */
	@SuppressWarnings("unused")
	abstract DeathRecord deleteRecord(final UUID playerUUID, final String worldName);


	/**
	 * Remove player from storage cache
	 *
	 * @param playerUUID player UUID to remove from storage cache
	 */
	abstract public void flushCache(final UUID playerUUID);


	/**
	 * Close storage
	 */
	public abstract void close();


	/**
	 * Save datastore to disk
	 */
	abstract void save();


	/**
	 * Delete datastore file
	 *
	 * @return boolean {@code true} if deletion was successful, {@code false} if unsuccessful
	 */
	@SuppressWarnings("UnusedReturnValue")
	abstract boolean delete();


	/**
	 * Check if datastore file exists
	 *
	 * @return {@code true} if file exists, {@code false} if file does not exist
	 */
	abstract boolean exists();


	/**
	 * Get datastore file name
	 *
	 * @return String containing file name for datastore
	 */
	String getFilename() {
		return this.filename;
	}


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType - the datastore type for this datastore
	 */
	@SuppressWarnings("WeakerAccess")
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name, formatted for display
	 *
	 * @return String containing datastore name
	 */
	@SuppressWarnings("WeakerAccess")
	public String getDisplayName() {
		return this.getType().toString();
	}


	/**
	 * Check if datastore is initialize
	 *
	 * @return {@code true} if datastore is initialized, {@code false} if datastore is not initialized
	 */
	boolean isInitialized() {
		return this.initialized;
	}


	/**
	 * Set initialized state of datastore
	 *
	 * @param initialized boolean for initialized state of datastore
	 */
	void setInitialized(final boolean initialized) {
		this.initialized = initialized;
	}


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	public static DataStore create() {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 *
	 * @param dataStoreType new datastore type
	 * @param oldDataStore  existing datastore reference
	 * @return a new datastore instance of the given type
	 */
	private static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {

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


	public static void reload() {

		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = create(newType, plugin.dataStore);
		}

	}


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	private static void convert(final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {

			plugin.getLogger().info("Converting existing " + oldDataStore.getDisplayName() + " datastore to "
					+ newDataStore.getDisplayName() + " datastore...");

			// initialize old datastore
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore.toString() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			List<DeathRecord> allRecords = oldDataStore.getAllRecords();

			int count = 0;
			for (DeathRecord record : allRecords) {
				newDataStore.putRecord(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to new datastore.");

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
	private static void convertAll(final DataStore newDataStore) {

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
