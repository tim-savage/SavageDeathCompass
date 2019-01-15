package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;


/**
 * Implements a death compass object for storage.
 * Provides static methods for creating and checking for death compass ItemStacks.
 */
public final class DeathRecord {

	// player UUID
	private final UUID playerUUID;

	// player death location
	private final Location location;

	// reference to plugin main class
	private final static PluginMain plugin = PluginMain.INSTANCE;


	/**
	 * Class constructor
	 *
	 * @param playerUUID the player UUID for the DeathCompass
	 * @param location   the player death location for the DeathCompass
	 */
	public DeathRecord(final UUID playerUUID, final Location location) {

		// test for null parameters
		Objects.requireNonNull(playerUUID);
//		Objects.requireNonNull(location);

		// set playerUUID
		this.playerUUID = playerUUID;

		// set player death location with defensive copy of passed location, or world spawn if location is null
		if (location == null) {
			plugin.getLogger().warning("DeathCompass constructor was passed null location!");
			this.location = plugin.getServer().getPlayer(playerUUID).getWorld().getSpawnLocation();
		}
		else {
			this.location = new Location(location.getWorld(),
					location.getX(),
					location.getY(),
					location.getZ(),
					location.getYaw(),
					location.getPitch());
		}
	}


	/**
	 * Getter for playerUUID
	 *
	 * @return UUID for death compass player
	 */
	final UUID getPlayerUUID() {

		// return playerUUID
		return this.playerUUID;
	}


	/**
	 * Getter for location
	 *
	 * @return Location for death compass player death location
	 */
	public final Location getLocation() {

		// return defensive copy of location
		return new Location(this.location.getWorld(),
				this.location.getX(),
				this.location.getY(),
				this.location.getZ(),
				this.location.getYaw(),
				this.location.getPitch());
	}

}
