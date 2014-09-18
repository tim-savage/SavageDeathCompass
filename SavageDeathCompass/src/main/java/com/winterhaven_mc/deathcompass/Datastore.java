package com.winterhaven_mc.deathcompass;

import org.bukkit.Location;
import org.bukkit.entity.Player;

abstract class Datastore {

	/**
	 * Initialize Datastore
	 * @throws Exception
	 */
	abstract void initializeDb();
	
	/**
	 * Retrieve player death location from Datastore
	 * @param player
	 * @return
	 * @throws Exception
	 */
	abstract Location getRecord(Player player);
	
	/**
	 * Write player death location into Datastore
	 * @param player
	 * @throws Exception
	 */
	abstract void putRecord(Player player);

	/**
	 * Close Datastore
	 * @throws Exception
	 */
	abstract void closeDb();

}
