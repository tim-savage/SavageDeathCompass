package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
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

	// player death location components
	private final UUID worldUid;
	private final double x;
	private final double y;
	private final double z;


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

		this.worldUid = Objects.requireNonNull(player.getLocation().getWorld()).getUID();
		this.x = player.getLocation().getX();
		this.y = player.getLocation().getY();
		this.z = player.getLocation().getZ();
	}


	public DeathRecord(final UUID playerUUID, final UUID worldUid, final double x, final double y, final double z) {
		this.playerUUID = playerUUID;
		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
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

		this.worldUid = Objects.requireNonNull(location.getWorld()).getUID();
		this.x = location.getX();
		this.y = location.getY();
		this.z = location.getZ();
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


	public final UUID getWorldUid() {
		return this.worldUid;
	}


	public final double getX() {
		return this.x;
	}


	public final double getY() {
		return this.y;
	}


	public final double getZ() {
		return this.z;
	}


	/**
	 * Getter for location
	 *
	 * @return Location - death compass player death location
	 */
	public final Location getLocation() {

		// get world from uid
		final World world = Bukkit.getServer().getWorld(this.worldUid);

		// if world is invalid, return null
		if (world == null) {
			return null;
		}

		// return new location object
		return new Location(world, this.x, this.y, this.z);
	}

}
