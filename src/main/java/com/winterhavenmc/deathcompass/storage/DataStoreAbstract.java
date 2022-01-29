package com.winterhavenmc.deathcompass.storage;


/**
 * Abstract class declares methods that need to be implemented for plugin data stores.
 */
abstract class DataStoreAbstract {

	// datastore initialized state
	private boolean initialized;

	// datastore type
	DataStoreType type;


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


	/**
	 * Get datastore type
	 *
	 * @return DataStoreType - the datastore type for this datastore
	 */
	public DataStoreType getType() {
		return this.type;
	}


	/**
	 * Override toString method to return the datastore type name
	 *
	 * @return the name of this datastore instance
	 */
	@Override
	public String toString() {
		return this.type.toString();
	}

}
