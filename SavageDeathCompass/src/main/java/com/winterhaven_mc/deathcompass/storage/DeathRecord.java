package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class DeathRecord {

	private final UUID playerUUID;
	private final Location location;

	
	/**
	 * Class constructor
	 * @param playerUUID the player UUID for the DeathRecord
	 * @param location the player death location for the DeathRecord
	 */
	DeathRecord(UUID playerUUID, Location location) {

		this.playerUUID = playerUUID;
		this.location = location;
	}
	

	/**
	 * Class constructor
	 * @param player the player whose UUID and location will be used to create the DeathRecord
	 */
	public DeathRecord(Player player) {
		
		// set playerUUID
		this.playerUUID = player.getUniqueId();

		// set location
		this.location = player.getLocation();
	}

	
	@SuppressWarnings("WeakerAccess")
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	
	public Location getLocation() {
		return location;
	}

}
