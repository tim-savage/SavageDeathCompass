package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * Implements in memory cache for datastore objects
 */
final class DeathRecordCache implements Listener {

	// static reference to plugin main class
	private final PluginMain plugin = PluginMain.INSTANCE;

	// death location map by player uuid, world uid -> death record
	private final Map<UUID, Map<UUID, DeathRecord>> deathRecordMap;


	/**
	 * Constructor
	 */
	DeathRecordCache() {

		// initialize location map
		deathRecordMap = new HashMap<>();

		// register events in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Insert death location into cache keyed by player UUID and world UID
	 *
	 * @param deathRecord object containing player UUID and death location to cache
	 */
	final void put(final DeathRecord deathRecord) {

		// check for null parameter
		Objects.requireNonNull(deathRecord);

		// get player UUID from death record
		UUID playerUUID = deathRecord.getPlayerUUID();

		// get death location from death record
		Location location = deathRecord.getLocation();

		// get world UID from death record location
		UUID worldUID = Objects.requireNonNull(location.getWorld()).getUID();

		// get map for player
		Map<UUID, DeathRecord> playerMap = deathRecordMap.get(playerUUID);

		// if no cached entry exists for player, create new map
		if (playerMap == null) {

			// create empty map
			playerMap = new HashMap<>();
		}

		// put this deathRecord into world map
		playerMap.put(worldUID, deathRecord);

		// put world map into player map
		deathRecordMap.put(playerUUID, playerMap);
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 *
	 * @param playerUUID player UUID to use as key
	 * @param worldUID   world UID to use as key
	 * @return deathRecord containing playerUUID and death location for world, or null if no record exists
	 */
	final DeathRecord get(final UUID playerUUID, final UUID worldUID) {

		// if passed playerUUID is null, return null record
		if (playerUUID == null) {
			if (plugin.debug) {
				plugin.getLogger().warning("LocationCache.get was passed null playerUUID!");
			}
			return null;
		}

		// if passed worldUID is null, return null record
		if (worldUID == null) {
			if (plugin.debug) {
				plugin.getLogger().warning("LocationCache.get was passed null worldUID!");
			}
			return null;
		}

		// if map for player does not exist, return null record
		if (deathRecordMap.get(playerUUID) == null) {
			return null;
		}

		// if location in map is null, return null record
		if (deathRecordMap.get(playerUUID).get(worldUID) == null) {
			return null;
		}

		// return record fetched from cache
		return deathRecordMap.get(playerUUID).get(worldUID);
	}


	/**
	 * Remove player from cache on player quit event
	 *
	 * @param event the event handled by this listener
	 */
	@EventHandler
	final void onPlayerQuit(PlayerQuitEvent event) {
		deathRecordMap.remove(event.getPlayer().getUniqueId());
	}
}
