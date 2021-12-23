package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;


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

	// static reference to main class
//	private final static JavaPlugin plugin = JavaPlugin.getPlugin(PluginMain.class);

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

}
