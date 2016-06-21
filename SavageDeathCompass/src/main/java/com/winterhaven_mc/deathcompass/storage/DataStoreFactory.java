package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class DataStoreFactory {
	
	private static PluginMain plugin = PluginMain.instance;



	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 * @return new datastore of configured type
	 */
	public static DataStore create() {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
		if (dataStoreType == null) {
			dataStoreType = DataStoreType.getDefaultType();
		}
		return create(dataStoreType, null);
	}


	/**
	 * Create new data store of given type and convert old data store.<br>
	 * Two parameter version used when a datastore instance already exists
	 * @param dataStoreType		new datastore type
	 * @param oldDataStore		existing datastore reference
	 * @return	reference to the new datastore
	 */
	private static DataStore create(final DataStoreType dataStoreType, final DataStore oldDataStore) {

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.create();

		// initialize new data store
		try {
			newDataStore.initialize();
		} catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore.getDisplayName() + " datastore!");
			if (plugin.debug) {
				e.printStackTrace();
			}
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
	 * convert old data store to new data store
	 * @param oldDataStore the old datastore to be converted from
	 * @param newDataStore the new datastore to be converted to
	 */
	private static void convertDataStore(DataStore oldDataStore, DataStore newDataStore) {

		// if datastores are same type, do not convert
		if (oldDataStore.getType().equals(newDataStore.getType())) {
			return;
		}
		
		// if old datastore file exists, attempt to read all records
		if (oldDataStore.exists()) {
			
			plugin.getLogger().info("Converting existing " + oldDataStore.getDisplayName() + " datastore to "
					+ newDataStore.getDisplayName() + " datastore...");
			
			// initialize old datastore
			if (!oldDataStore.isInitialized()) {
				try {
					oldDataStore.initialize();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not initialize "
							+ oldDataStore.toString() + " datastore for conversion.");
					plugin.getLogger().warning(e.getLocalizedMessage());
					return;
				}
			}
			
			List<DeathRecord> allRecords = oldDataStore.getAllRecords();
			
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


	/**
	 * convert all existing data stores to new data store
	 * @param newDataStore the datastore to convert all other existing datastore to
	 */
	private static void convertAll(final DataStore newDataStore) {

		// get array list of all data store types
		ArrayList<DataStoreType> dataStoresTypes = new ArrayList<DataStoreType>(Arrays.asList(DataStoreType.values()));

		// remove newDataStore from list of types to convert
		dataStoresTypes.remove(newDataStore.getType());

		for (DataStoreType type : dataStoresTypes) {

			// create oldDataStore holder
			DataStore oldDataStore = type.create();

			if (oldDataStore != null && oldDataStore.exists()) {

				convertDataStore(oldDataStore, newDataStore);
			}
		}
	}


	/**
	 * Reload the datastore, converting to new type if configured datastore type has changed
	 */
	public static void reload() {
		
		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();
		
		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));
				
		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// save current datastore
			plugin.dataStore.save();

			// create new datastore
			plugin.dataStore = create(newType, plugin.dataStore);
		}
	}

}
