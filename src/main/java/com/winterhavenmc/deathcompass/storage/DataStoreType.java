package com.winterhavenmc.deathcompass.storage;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Enum of datastore types available for use by plugin
 */
public enum DataStoreType {

	SQLITE("SQLite", "deathlocations.db") {

		@Override
		public DataStore connect(JavaPlugin plugin) {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}


		@Override
		boolean storageObjectExists(JavaPlugin plugin) {
			// get path name to data store file
			File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getStorageName());
			return dataStoreFile.exists();
		}
	};


	// DataStoreType display name
	private final String displayName;

	// data store object name
	private final String storageName;

	// default DataStoreType
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 *
	 * @param displayName the display name of the datastore type
	 */
	DataStoreType(final String displayName, final String storageName) {
		this.displayName = displayName;
		this.storageName = storageName;
	}


	/**
	 * Getter method for DataStoreType display name
	 *
	 * @return String containing DataStoreType display name
	 */
	public String getDisplayName() {
		return displayName;
	}


	/**
	 * Getter for storage object name.
	 *
	 * @return the name of the backing store object for a data store type
	 */
	String getStorageName() {
		return storageName;
	}


	/**
	 * Test if datastore backing object (file, database) exists
	 *
	 * @param plugin reference to plugin main class
	 * @return true if backing object exists, false if not
	 */
	abstract boolean storageObjectExists(JavaPlugin plugin);


	/**
	 * Get display name of DataStoreType
	 *
	 * @return String - display name of DataStoreType
	 */
	@Override
	public String toString() {
		return displayName;
	}


	/**
	 * Attempt to match datastore type to passed name string
	 *
	 * @param name datastore name to match
	 * @return matched DataStoreType, or default type if no match
	 */
	public static DataStoreType match(final String name) {

		// try to match data store type to name
		for (DataStoreType type : DataStoreType.values()) {
			if (type.getDisplayName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return sqlite
		return defaultType;
	}


	/**
	 * Abstract method for DataStore creation method
	 *
	 * @return new DataStore
	 */
	public abstract DataStore connect(JavaPlugin plugin);


	/**
	 * convert old data store to new data store
	 *
	 * @param oldDataStore the old datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	static void convert(final JavaPlugin plugin, final DataStore oldDataStore, final DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}

		// if old datastore file exists, attempt to read all records
		if (oldDataStore.getType().storageObjectExists(plugin)) {

			plugin.getLogger().info("Converting existing " + oldDataStore + " datastore to "
					+ newDataStore + " datastore...");

			// initialize old datastore
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				}
				catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}

			// get count of records inserted in new datastore
			int count = newDataStore.insertRecords(oldDataStore.selectAllRecords());

			// log record count message
			plugin.getLogger().info(count + " records converted to new datastore.");

			// flush new datastore to disk if applicable
			newDataStore.sync();

			// close old datastore
			oldDataStore.close();

			// delete old datastore storage object
			oldDataStore.delete();
		}
	}


	/**
	 * convert all existing data stores to new data store
	 *
	 * @param newDataStore the datastore to convert all other existing datastore to
	 */
	static void convertAll(JavaPlugin plugin, final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStoresTypes = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStoresTypes.remove(newDataStore.getType());

		for (DataStoreType type : dataStoresTypes) {
			if (type.storageObjectExists(plugin)) {
				convert(plugin, type.connect(plugin), newDataStore);
			}
		}
	}

}
