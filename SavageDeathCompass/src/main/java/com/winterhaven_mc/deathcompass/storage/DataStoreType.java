package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;


/**
 * Enum of datastore types available for use by plugin
 */
public enum DataStoreType {

	SQLITE("SQLite") {
		@Override
		public DataStore create(JavaPlugin plugin) {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};


	// DataStoreType display name
	private final String displayName;

	// default DataStoreType
	private final static DataStoreType defaultType = DataStoreType.SQLITE;


	/**
	 * Class constructor
	 *
	 * @param displayName the display name of the datastore type
	 */
	DataStoreType(final String displayName) {
		this.displayName = displayName;
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
	 * Static getter method for default DataStoreType
	 *
	 * @return default DataStoreType
	 */
	public static DataStoreType getDefaultType() {
		return defaultType;
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
		return DataStoreType.SQLITE;
	}


	/**
	 * Abstract method for DataStore creation method
	 *
	 * @return new DataStore
	 */
	public abstract DataStore create(JavaPlugin plugin);


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

			int count = newDataStore.insertRecords(allRecords);

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
	static void convertAll(JavaPlugin plugin, final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStoresTypes = new ArrayList<>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStoresTypes.remove(newDataStore.getType());

		for (DataStoreType type : dataStoresTypes) {

			// create oldDataStore holder
			DataStore oldDataStore = type.create(plugin);

			if (oldDataStore != null && oldDataStore.exists()) {

				convert(oldDataStore, newDataStore);
			}
		}
	}

}
