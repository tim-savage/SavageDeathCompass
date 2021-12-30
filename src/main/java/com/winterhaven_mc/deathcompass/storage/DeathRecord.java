package com.winterhaven_mc.deathcompass.storage;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;


/**
 * Implements a death record object for storage.
 */
public final class DeathRecord {

	// player UUID
	private final UUID playerUid;

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
		this.playerUid = player.getUniqueId();

		// set location components
		this.worldUid = Objects.requireNonNull(player.getLocation().getWorld()).getUID();
		this.x = player.getLocation().getX();
		this.y = player.getLocation().getY();
		this.z = player.getLocation().getZ();
	}


	public DeathRecord(final UUID playerUid, final UUID worldUid, final double x, final double y, final double z) {
		this.playerUid = playerUid;
		this.worldUid = worldUid;
		this.x = x;
		this.y = y;
		this.z = z;
	}


	/**
	 * Getter for playerUid
	 *
	 * @return UUID for death record player
	 */
	UUID getPlayerUid() {
		return this.playerUid;
	}


	/**
	 * Getter for worldUid
	 *
	 * @return UUID for death record world
	 */
	public UUID getWorldUid() {
		return this.worldUid;
	}


	/**
	 * Getter for x coordinate
	 *
	 * @return x coordinate
	 */
	public double getX() {
		return this.x;
	}


	/**
	 * Getter for y coordinate
	 *
	 * @return y coordinate
	 */
	public double getY() {
		return this.y;
	}


	/**
	 * Getter for z coordinate
	 *
	 * @return z coordinate
	 */
	public double getZ() {
		return this.z;
	}


	/**
	 * Getter for location
	 *
	 * @return Location - death compass player death location
	 */
	public Location getLocation() {

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
