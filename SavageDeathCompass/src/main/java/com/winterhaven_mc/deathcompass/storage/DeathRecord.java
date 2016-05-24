package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class DeathRecord {

	private UUID playerUUID;
	private Location location;

	
	/**
	 * Class constructor
	 * @param playerUUID
	 * @param location
	 */
	DeathRecord(UUID playerUUID, Location location) {
		
		setPlayerUUID(playerUUID);
		setLocation(location);	
	}
	

	/**
	 * Class constructor
	 * @param player
	 */
	public DeathRecord(Player player) {
		
		// set playerUUID
		setPlayerUUID(player.getUniqueId());
		
		// set location
		setLocation(player.getLocation());
	}

	
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	
	public void setPlayerUUID(UUID playerUUID) {
		this.playerUUID = playerUUID;
	}

	
	public Location getLocation() {
		return location;
	}
	

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
