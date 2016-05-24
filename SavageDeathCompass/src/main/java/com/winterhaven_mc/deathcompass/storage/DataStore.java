package com.winterhaven_mc.deathcompass.storage;

import java.util.List;
import java.util.UUID;

public abstract class DataStore {

	protected boolean initialized;

	protected DataStoreType type;

	protected String filename;


	/**
	 * Initialize storage
	 * @throws Exception
	 */
	abstract void initialize() throws Exception;

	/**
	 * Get record
	 * @param playerUUID
	 * @param worldName
	 * @return death record or null if no matching record
	 */
	public abstract DeathRecord getRecord(UUID playerUUID, String worldName);

	/**
	 * Store record
	 * @param deathRecord
	 */
	public abstract void putRecord(DeathRecord deathRecord);

	/**
	 * get all records
	 * @return
	 */
	abstract List<DeathRecord> getAllRecords();

	/**
	 * Delete record
	 * @param playerUUID
	 * @param worldName
	 * @return 
	 */	
	abstract DeathRecord deleteRecord(UUID playerUUID, String worldName);

	/**
	 * Close storage
	 */
	public abstract void close();

	abstract void save();

	abstract void delete();

	abstract boolean exists();

	String getFilename() {
		return this.filename;
	}

	public DataStoreType getType() {
		return this.type;
	}

	public String getName() {
		return this.getType().getName();
	}

	boolean isInitialized() {
		return this.initialized;
	}

	void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

}
