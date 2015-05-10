package com.winterhaven_mc.deathcompass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataStoreFactory {
	
	static DeathCompassMain plugin = DeathCompassMain.instance;

	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore instance exists
	 * and new datastore type is to be read from configuration file
	 * @return new datastore of configured type
	 */
	static DataStore create() {
		
		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.SQLITE;
		}
		return create(dataStoreType, null);
	}
	
	/**
	 * Create new data store of given type.<br>
	 * Single parameter version used when no current datastore instance exists
	 * and new datastore type is passed as parameter
	 * @param dataStoreType
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType) {
		return create(dataStoreType, null);
	}
	
	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 * @param dataStoreType
	 * @param oldDataStore
	 * @return
	 */
	static DataStore create(DataStoreType dataStoreType, DataStore oldDataStore) {
	
		DataStore newDataStore = null;
		
		// if datastore type yaml is specified
		if (dataStoreType.equals(DataStoreType.YAML)) {
			
			// get initialized yaml datastore
			newDataStore = createYaml();
		}
		
		// add new datastore types here as they become available
		
		else {
			// get initialized sqlite datastore
			newDataStore = createSqlite();
		}
		
		// if old data store was passed, convert to new data store
		if (oldDataStore != null) {
			convertDataStore(oldDataStore, newDataStore);
		}
		else {
			convertAll(newDataStore);
		}
		// return initialized data store
		return newDataStore;
	}

	
	/**
	 * try to create new yaml datastore; disable plugin on failure
	 * @return
	 */
	static DataStore createYaml() {

		// create new yaml datastore object
		DataStore newDataStore = new DataStoreYAML(plugin);

		// initialize yaml datastore
		try {
			newDataStore.initialize();
		}
		catch (IllegalArgumentException e) {
			// cannot load embedded default file. Not a deal-breaker, so just log it.
			plugin.getLogger().warning(e.getLocalizedMessage());
		}
		catch (IllegalStateException e) {
			// cannot access plugin data folder. this is critical, so disable plugin.
			plugin.getLogger().severe("Cannot access plugin data folder. Disabling plugin.");
			plugin.getPluginLoader().disablePlugin(plugin);
			return null;
		}
		catch (Exception e) {
			// unforeseen error initializing yaml datastore, so disable plugin.
			plugin.getLogger().severe("An error occurred while trying to initialize the yaml datastore.");
			plugin.getLogger().severe(e.getLocalizedMessage());
			plugin.getLogger().severe("Disabling plugin...");
			plugin.getPluginLoader().disablePlugin(plugin);
			return null;
		}
		return newDataStore;
	}

	
	/**
	 * try to create new sqlite data store; create yaml data store on failure
	 * @return
	 */
	static DataStore createSqlite() {

		// create new sqlite datastore object
		DataStore newDataStore = new DataStoreSQLite(plugin);

		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			// error initializing sqlite datastore, so try yaml as fallback
			plugin.getLogger().warning("An error occurred while trying to initialize the sqlite datastore.");
			plugin.getLogger().warning(e.getLocalizedMessage());
			plugin.getLogger().warning("Trying yaml datastore as fallback...");
			newDataStore = createYaml();
		}
		return newDataStore;
	}

	
	/**
	 * convert old data store to new data store
	 * @param oldDataStore
	 * @param newDataStore
	 */
	static void convertDataStore(DataStore oldDataStore, DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			if (plugin.debug) {
				plugin.getLogger().info("Old and new datastore both " 
						+ newDataStore.getName() + ". No conversion necessary.");
			}
			return;
		}
		
		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {
			
			plugin.getLogger().info("Converting existing " + oldDataStore.getName() + " datastore to "
					+ newDataStore.getName() + " datastore...");
			
			// initialize old datastore
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize " + oldDataStore.getName() + " datastore!");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}
			
			List<DeathRecord> allRecords = new ArrayList<DeathRecord>();
			
			allRecords = oldDataStore.getAllRecords();
			
			int count = 0;
			for (DeathRecord record : allRecords) {
				newDataStore.putRecord(record);
				count++;
			}
			plugin.getLogger().info(count + " records converted to new datastore.");
			
			newDataStore.save();
			
			oldDataStore.close();
			oldDataStore.delete();
		}
	}
	
//	/**
//	 * convert any existing data store to new data store
//	 * @param newDataStore
//	 */
//	static void convertAny(DataStore newDataStore) {
//		
//		if (newDataStore.getType().equals(DataStoreType.YAML)) {
//			
//			if (plugin.debug) {
//				plugin.getLogger().info("Attempting to convert existing datastore to yaml...");
//			}
//			
//			DataStore oldDataStore = new DataStoreSQLite(plugin);
//			convertDataStore(oldDataStore, newDataStore);
//		}
//		else if (newDataStore.getType().equals(DataStoreType.SQLITE)) {
//			
//			if (plugin.debug) {
//				plugin.getLogger().info("Attempting to convert existing datastore to sqlite...");
//			}
//			
//			DataStore oldDataStore = new DataStoreYAML(plugin);
//			convertDataStore(oldDataStore, newDataStore);
//		}
//	}

	
	/**
	 * convert all existing data stores to new data store
	 * @param newDataStore
	 */
	static void convertAll(DataStore newDataStore) {
		
		// get array list of all data store types
		ArrayList<DataStoreType> dataStores = new ArrayList<DataStoreType>(Arrays.asList(DataStoreType.values()));
		
		// remove newDataStore from list of types to convert
		dataStores.remove(newDataStore);
		
		for (DataStoreType type : dataStores) {

			// create oldDataStore holder
			DataStore oldDataStore = null;
			
			if (type.equals(DataStoreType.YAML)) {
				oldDataStore = new DataStoreYAML(plugin);
			}
			else if (type.equals(DataStoreType.SQLITE)) {
				oldDataStore = new DataStoreSQLite(plugin);
			}
			// add additional datastore types here as they become available
			
			if (oldDataStore != null) {
				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}
	
}
