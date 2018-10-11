package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


class LocationCache {

	// death location cache by player uuid, world uid -> death location
	private Map<UUID,Map<UUID,Location>> locationCache = new HashMap<UUID, Map<UUID, Location>>();


	/**
	 * Insert death location into cache keyed by player UUID and world UID
	 * @param deathRecord object containing player UUID and death location to cache
	 */
	void cacheLocation(DeathCompass deathRecord) {

		// if death record (or any element) is null, do nothing and return
		if (deathRecord == null
				|| deathRecord.getPlayerUUID() == null
				|| deathRecord.getLocation() == null) {
			return;
		}

		// get player UUID from death record
		UUID playerUUID = deathRecord.getPlayerUUID();

		// get death location from death record
		Location location = deathRecord.getLocation();

		// get world UID from world referenced by death record location
		UUID worldUID = location.getWorld().getUID();

		// get world map for player
		Map<UUID,Location> worldMap = locationCache.get(playerUUID);

		// if no cache entry exists for player, create new world map
		if (worldMap == null) {

			// create empty world map
			worldMap = new HashMap<UUID, Location>();
		}

		// put this location into world map
		worldMap.put(worldUID,location);

		// put world map into player map
		locationCache.put(playerUUID,worldMap);

		// output debug message if configured
		if (PluginMain.instance.debug) {
			PluginMain.instance.getLogger().info("Death location cached for player UUID " + playerUUID.toString()
					+ " in world " + worldUID.toString());
		}
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 * @param playerUUID player UUID to use as key
	 * @param worldUID world UID to use as key
	 * @return deathRecord containing playerUUID and death location for world
	 */
	DeathCompass fetchLocation(UUID playerUUID, UUID worldUID) {

		// if any passed arguments are null, return null record
		if (playerUUID == null || worldUID == null) {
			return null;
		}

		// if map for player does not exist, return null record
		if (locationCache.get(playerUUID) == null) {
			return null;
		}

		// output debug message if configured
		if (PluginMain.instance.debug) {
			PluginMain.instance.getLogger().info("Death location fetched for player UUID " + playerUUID.toString()
				+ " in world " + worldUID.toString());
		}

		// return record fetched from cache (may be null)
		return new DeathCompass(playerUUID,locationCache.get(playerUUID).get(worldUID));
	}


	/**
	 * Remove player UUID from location cache
	 * @param playerUUID the player UUID to be removed from location cache
	 */
	void flushPlayerMap(UUID playerUUID) {

		// if player uuid is null, do nothing and return
		if (playerUUID == null) {
			return;
		}

		// remove player uuid from location cache
		locationCache.remove(playerUUID);

		// output debug message if configured
		if (PluginMain.instance.debug) {
			PluginMain.instance.getLogger().info("Player UUID " + playerUUID.toString() + " removed from cache.");
		}
	}
}
