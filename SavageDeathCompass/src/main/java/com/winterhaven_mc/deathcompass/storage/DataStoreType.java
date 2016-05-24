package com.winterhaven_mc.deathcompass.storage;

public enum DataStoreType {

	YAML("yaml"),
	SQLITE("SQLite");

	private String name;
	
	/**
	 * Class constructor
	 * @param name the display name of the datastore type
	 */
	DataStoreType(String name) {
		this.setName(name);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public static DataStoreType match(String name) {
		
		// account for old config setting 'file', map to yaml
		if (name.equalsIgnoreCase("file")) {
			return DataStoreType.YAML;
		}
		
		// try to match data store type to name
		for (DataStoreType type : DataStoreType.values()) {
			if (type.getName().equalsIgnoreCase(name)) {
				return type;
			}
		}
		// no match; return sqlite
		return DataStoreType.SQLITE;
	}

}
