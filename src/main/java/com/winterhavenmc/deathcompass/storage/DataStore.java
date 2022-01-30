/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathcompass.storage;

import com.winterhavenmc.deathcompass.PluginMain;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.UUID;

public interface DataStore {

	/**
	 * Initialize storage
	 */
	void initialize() throws Exception;


	/**
	 * Check if datastore is initialized
	 * @return boolean - true if initialized, false if not
	 */
	boolean isInitialized();


	/**
	 * Get enum member representing datastore type
	 * @return DataStoreType
	 */
	DataStoreType getType();


	/**
	 * get all records from datastore
	 *
	 * @return List of all DeathRecords
	 */
	Collection<DeathRecord> selectAllRecords();


	/**
	 * Get record from datastore
	 *
	 * @param playerUUID the player UUID of the record to be retrieved
	 * @param worldUID   the world UID of the record to be retrieved
	 * @return death record or null if no matching record found
	 */
	DeathRecord selectRecord(final UUID playerUUID, final UUID worldUID);


	/**
	 * Insert a record in datastore
	 *
	 * @param deathRecord a DeathRecord to be inserted
	 */
	void insertRecord(final DeathRecord deathRecord);


	/**
	 * Insert records in datastore
	 *
	 * @param deathRecords a collection of DeathRecords to be inserted
	 * @return int number of records inserted
	 */
	int insertRecords(final Collection<DeathRecord> deathRecords);


	/**
	 * Delete record
	 *
	 * @param playerUUID the player uuid of the record to delete
	 * @param worldUID   the world uid of the record to delete
	 * @return the DeathRecord that was deleted from datastore
	 */
	@SuppressWarnings("unused")
	DeathRecord deleteRecord(final UUID playerUUID, final UUID worldUID);


	/**
	 * Close storage
	 */
	void close();


	/**
	 * Save datastore to disk, if applicable
	 */
	void sync();


	/**
	 * Delete datastore file
	 * @return true if deletion successful, else false
	 */
	@SuppressWarnings("UnusedReturnValue")
	boolean delete();


	/**
	 * Create new data store of given type.<br>
	 * No parameter version used when no current datastore exists
	 * and datastore type should be read from configuration
	 *
	 * @return new datastore of configured type
	 */
	static DataStore connect(JavaPlugin plugin) {

		// get data store type from config
		DataStoreType dataStoreType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// get new data store of specified type
		DataStore newDataStore = dataStoreType.connect(plugin);

		// initialize new data store
		try {
			newDataStore.initialize();
		}
		catch (Exception e) {
			plugin.getLogger().severe("Could not initialize " + newDataStore + " datastore!");
			plugin.getLogger().severe(e.getLocalizedMessage());
			if (plugin.getConfig().getBoolean("debug")) {
				e.printStackTrace();
			}
		}

		// convert any existing data stores to new type
		DataStoreType.convertAll(plugin, newDataStore);

		// return initialized data store
		return newDataStore;
	}


	/**
	 * Reload datastore, if configuration has changed
	 *
	 * @param plugin reference to plugin main class
	 */
	static void reload(PluginMain plugin) {

		// get current datastore type
		DataStoreType currentType = plugin.dataStore.getType();

		// get configured datastore type
		DataStoreType newType = DataStoreType.match(plugin.getConfig().getString("storage-type"));

		// if current datastore type does not match configured datastore type, create new datastore
		if (!currentType.equals(newType)) {

			// create new datastore
			plugin.dataStore = connect(plugin);
		}
	}

}
