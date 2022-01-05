package com.winterhavenmc.deathcompass.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * Implements in memory cache for datastore objects
 */
final class DeathRecordCache implements Listener {

	// static reference to plugin main class
	private final JavaPlugin plugin;

	// death location map by player uuid, world uid -> death record
	private final Map<UUID, Map<UUID, DeathRecord>> deathRecordMap;


	/**
	 * Constructor
	 */
	DeathRecordCache(final JavaPlugin plugin) {

		this.plugin = plugin;

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
	void put(final DeathRecord deathRecord) {

		// check for null parameter
		Objects.requireNonNull(deathRecord);

		// get player UUID from death record
		final UUID playerUid = deathRecord.getPlayerUid();

		// get world UUID from death record location
		final UUID worldUid = deathRecord.getWorldUid();

		// get map for player
		Map<UUID, DeathRecord> playerMap = deathRecordMap.get(playerUid);

		// if no cached entry exists for player, create new map
		if (playerMap == null) {

			// create empty map
			playerMap = new HashMap<>();
		}

		// put this deathRecord into world map
		playerMap.put(worldUid, deathRecord);

		// put world map into player map
		deathRecordMap.put(playerUid, playerMap);
	}


	/**
	 * Fetch death record for player uuid / world uuid
	 *
	 * @param playerUid player UUID to use as key
	 * @param worldUid  world UID to use as key
	 * @return deathRecord containing playerUid and death location for world, or null if no record exists
	 */
	DeathRecord get(final UUID playerUid, final UUID worldUid) {

		// if passed playerUid is null, return null record
		if (playerUid == null) {
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().warning("LocationCache.get was passed null playerUid!");
			}
			return null;
		}

		// if passed worldUid is null, return null record
		if (worldUid == null) {
			if (plugin.getConfig().getBoolean("debug")) {
				plugin.getLogger().warning("LocationCache.get was passed null worldUid!");
			}
			return null;
		}

		// if map for player does not exist, return null record
		if (deathRecordMap.get(playerUid) == null) {
			return null;
		}

		// if location in map is null, return null record
		if (deathRecordMap.get(playerUid).get(worldUid) == null) {
			return null;
		}

		// return record fetched from cache
		return deathRecordMap.get(playerUid).get(worldUid);
	}


	/**
	 * Remove player from cache on player quit event
	 *
	 * @param event the event handled by this listener
	 */
	@EventHandler
	void onPlayerQuit(final PlayerQuitEvent event) {
		deathRecordMap.remove(event.getPlayer().getUniqueId());
	}

}
