package com.winterhaven_mc.deathcompass;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathRecord {

	DeathCompassMain plugin = DeathCompassMain.instance;
	
	private String playerId;
	private Location location;
	
	/**
	 * Class constructor
	 * @param playerId
	 * @param location
	 */
	DeathRecord(String playerId, Location location) {
		
		setPlayerId(playerId);
		setLocation(location);	
	}
	
	/**
	 * Class constructor
	 * @param player
	 */
	DeathRecord(Player player) {
		
		// if use-uuid configured, set playerId to uuid; else use player name
		if (plugin.getConfig().getBoolean("use-uuid")) {
			setPlayerId(player.getUniqueId().toString());
		}
		else {
		setPlayerId(player.getName());
		}
		setLocation(player.getLocation());
	}

	
	public String getPlayerId() {
		return playerId;
	}
	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	
}
