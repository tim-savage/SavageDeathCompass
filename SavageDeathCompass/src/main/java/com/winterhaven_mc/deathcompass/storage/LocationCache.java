package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


class LocationCache {

	// death location cache by world uid, player uid -> death location
	private Map<UUID,Map<UUID,Location>> locationCache = new HashMap<UUID, Map<UUID, Location>>();

	void cacheLocation(DeathRecord deathRecord) {

		// if death record (or any element) is null, do nothing and return
		if (deathRecord == null
				|| deathRecord.getPlayerUUID() == null
				|| deathRecord.getLocation() == null) {
			return;
		}

		UUID playerUUID = deathRecord.getPlayerUUID();
		Location location = deathRecord.getLocation();
		UUID worldUID = location.getWorld().getUID();

		Map<UUID,Location> playerMap;

		// if no cache entry exists for world, create new player map
		if (locationCache.get(worldUID) == null) {

			// create empty player map
			playerMap = new HashMap<UUID, Location>();
		}
		else {
			// get player map for world
			playerMap = locationCache.get(worldUID);
		}

		// put this player record into player map
		playerMap.put(playerUUID,location);

		// put player map into world map
		locationCache.put(worldUID,playerMap);
	}


	DeathRecord fetchLocation(UUID worldUID, UUID playerUUID) {

		// if any passed arguments are null, return null record
		if (worldUID == null || playerUUID == null) {
			return null;
		}

		// if map for world does not exist, return null record
		if (locationCache.get(worldUID) == null) {
			return null;
		}

		// return record fetched from cache (may be null)
		return new DeathRecord(playerUUID,locationCache.get(worldUID).get(playerUUID));
	}

}
