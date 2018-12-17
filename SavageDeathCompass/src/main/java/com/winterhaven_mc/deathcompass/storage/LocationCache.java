package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


class LocationCache {

	// death location map by player uuid, world uid -> death location
	private Map<UUID,Map<UUID,Location>> locationMap;


	/**
	 * Constructor
	 */
	LocationCache() {

		// initialize location map
		locationMap = new HashMap<>();
	}


	/**
	 * Insert death location into cache keyed by player UUID and world UID
	 * @param deathRecord object containing player UUID and death location to cache
	 */
	void put(final DeathCompass deathRecord) {

		// if death record (or any element) is null, do nothing and return
		if (deathRecord == null) {
			return;
		}

		// get player UUID from death record
		UUID playerUUID = deathRecord.getPlayerUUID();

		// get death location from death record
		Location location = deathRecord.getLocation();

		// get world UID from world referenced by death record location
		UUID worldUID = location.getWorld().getUID();

		// get world map for player
		Map<UUID,Location> worldMap = locationMap.get(playerUUID);

		// if no cache entry exists for player, create new world map
		if (worldMap == null) {

			// create empty world map
			worldMap = new HashMap<>();
		}

		// put this location into world map
		worldMap.put(worldUID,location);

		// put world map into player map
		locationMap.put(playerUUID,worldMap);
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 * @param playerUUID player UUID to use as key
	 * @param worldUID world UID to use as key
	 * @return deathRecord containing playerUUID and death location for world, or null if no record exists
	 */
	DeathCompass get(final UUID playerUUID, final UUID worldUID) {

		// if any passed arguments are null, return null record
		if (playerUUID == null || worldUID == null) {
			return null;
		}

		// if map for player does not exist, return null record
		if (locationMap.get(playerUUID) == null) {
			return null;
		}

		// return record fetched from cache (may be null)
		return new DeathCompass(playerUUID, locationMap.get(playerUUID).get(worldUID));
	}


	/**
	 * Remove player UUID from location cache
	 * @param playerUUID the player UUID to be removed from location cache
	 */
	void removePlayer(final UUID playerUUID) {

		// if player uuid is null, do nothing and return
		if (playerUUID == null) {
			return;
		}

		// remove player uuid from location cache
		locationMap.remove(playerUUID);
	}
}
