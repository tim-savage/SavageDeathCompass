package com.winterhaven_mc.deathcompass;

import java.util.List;

public abstract class DataStore {

	protected boolean initialized;
	
	/**
	 * Initialize storage
	 * @throws Exception
	 */
	abstract void initialize() throws Exception;
	
	/**
	 * Get record
	 * @param playerId
	 * @param worldName
	 * @return death record or null if no matching record
	 */
	abstract DeathRecord getRecord(String playerId, String worldName);
	
	/**
	 * Store record
	 * @param destination
	 */
	abstract void putRecord(DeathRecord deathRecord);

	/**
	 * get all records
	 * @return
	 */
	abstract List<DeathRecord> getAllRecords();

	/**
	 * Delete record
	 * @param warpName
	 * @return 
	 */	
	abstract DeathRecord deleteRecord(String playerId, String worldName);
	
	/**
	 * Close storage
	 */
	abstract void close();

	abstract void save();
	
	abstract void delete();
	
	abstract boolean exists();
	
	abstract String getFilename();
	
	abstract DataStoreType getType();
	
	String getName() {
		return this.getType().getName();
	}
	
	boolean isInitialized() {
		return this.initialized;
	}
	
	void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
}
