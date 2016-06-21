package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

public enum DataStoreType {

	YAML("yaml") {

		@Override
		public DataStore create() {

			// create new yaml datastore
			return new DataStoreYAML(plugin);
		}
	},

	SQLITE("SQLite") {

		@Override
		public DataStore create() {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	private final static PluginMain plugin = PluginMain.instance;

	private String displayName;

	private final static DataStoreType defaultType = DataStoreType.SQLITE;

	/**
	 * Class constructor
	 * @param displayName the display name of the datastore type
	 */
	DataStoreType(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static DataStoreType getDefaultType() {
		return defaultType;
	}

	public static DataStoreType match(String name) {
		
		// account for old config setting 'file', map to yaml
		if (name.equalsIgnoreCase("file")) {
			return DataStoreType.YAML;
		}
		
		// try to match data store type to name
		for (DataStoreType type : DataStoreType.values()) {
			if (type.getDisplayName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return sqlite
		return DataStoreType.SQLITE;
	}

	public abstract DataStore create();

}
