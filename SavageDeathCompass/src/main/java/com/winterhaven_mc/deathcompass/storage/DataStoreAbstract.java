package com.winterhaven_mc.deathcompass.storage;


/**
 * Abstract class declares methods that need to be implemented for plugin data stores.
 */
public abstract class DataStoreAbstract {

	// datastore initialized state
	private boolean initialized;

	// datastore type
	DataStoreType type;

	// datastore filename
	String filename;


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
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Get datastore name, formatted for display
	 *
	 * @return String containing datastore name
	 */
	public String getDisplayName() {
		return this.getType().toString();
	}


	/**
	 * Check if datastore is initialize
	 *
	 * @return {@code true} if datastore is initialized, {@code false} if datastore is not initialized
	 */
	public boolean isInitialized() {
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

}
