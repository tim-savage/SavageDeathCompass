package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

public enum DataStoreType {

	SQLITE("SQLite") {

		@Override
		public DataStore create() {

			// create new sqlite datastore object
			return new DataStoreSQLite(plugin);
		}
	};

	private final static PluginMain plugin = PluginMain.INSTANCE;

	private String displayName;

	private final static DataStoreType defaultType = DataStoreType.SQLITE;

	/**
	 * Class constructor
	 * @param displayName the display name of the datastore type
	 */
	DataStoreType(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static DataStoreType getDefaultType() {
		return defaultType;
	}

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

	public abstract DataStore create();

}
