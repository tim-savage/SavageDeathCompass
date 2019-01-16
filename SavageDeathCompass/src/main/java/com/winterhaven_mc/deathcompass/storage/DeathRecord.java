package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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


	/**
	 * Class constructor
	 *
	 * @param player the player for the DeathRecord
	 */
	public DeathRecord(final Player player) {

		// test for null parameters
		Objects.requireNonNull(player);

		// set playerUUID
		this.playerUUID = player.getUniqueId();

		// set player death location with defensive copy of player location
		this.location = new Location(player.getLocation().getWorld(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ(),
				player.getLocation().getYaw(),
				player.getLocation().getPitch());
	}


	/**
	 * Class constructor
	 *
	 * @param playerUUID the player UUID for the DeathRecord
	 * @param location   the player death location for the DeathRecord
	 */
	DeathRecord(final UUID playerUUID, final Location location) {

		// test for null parameters
		Objects.requireNonNull(playerUUID);
		Objects.requireNonNull(location);

		// set playerUUID
		this.playerUUID = playerUUID;

		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
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
	 * @return Location - death compass player death location
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
