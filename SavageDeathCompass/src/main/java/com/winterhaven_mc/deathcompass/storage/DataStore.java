package com.winterhaven_mc.deathcompass.storage;

import java.util.List;
import java.util.UUID;


/**
 * Abstract class declares methods that need to be implemented for plugin data stores.
 */
public abstract class DataStore {

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
	public abstract DeathCompass getRecord(final UUID playerUUID, final String worldName);


	/**
	 * Store record in datastore
	 *
	 * @param deathRecord the DeathRecord to be stored
	 */
	public abstract void putRecord(final DeathCompass deathRecord);


	/**
	 * get all records from datastore
	 *
	 * @return List of all DeathRecords
	 */
	abstract List<DeathCompass> getAllRecords();


	/**
	 * Delete record
	 *
	 * @param playerUUID the player uuid of the record to delete
	 * @param worldName  the world name of the record to delete
	 * @return the DeathRecord that was deleted from datastore
	 */
	@SuppressWarnings("unused")
	abstract DeathCompass deleteRecord(final UUID playerUUID, final String worldName);


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

}
