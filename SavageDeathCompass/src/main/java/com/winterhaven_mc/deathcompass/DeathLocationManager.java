package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.ConfigAccessor;
import com.winterhaven_mc.deathcompass.DeathCompassMain;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Death location manager class
 * @author Tim Savage
 *
 */
public class DeathLocationManager {

	private final DeathCompassMain plugin; // pointer to main class

	private ConfigAccessor deathlocationsfile;
	
	private HashMap<String, HashMap<String, Location>> playerdeathlocations = new HashMap<String, HashMap<String, Location>>();

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public DeathLocationManager(DeathCompassMain plugin) {
		this.plugin = plugin;
		deathlocationsfile = new ConfigAccessor((JavaPlugin)plugin, "deathlocations.yml");
	}

	
	/**
	 * Set death location in hashmap and save to datastore file
	 * @param player
	 */
	public void setDeathLocation(Player player) {
		
		// set playerid to player name
		String playerid = player.getName();
		
		// if use-uuid is enabled in config, set playerid to player uuid
		if (this.plugin.getConfig().getBoolean("use-uuid", true)) {
			playerid = player.getUniqueId().toString();
		}
		String worldname = player.getWorld().getName();
		HashMap<String, Location> tempmap = new HashMap<String, Location>();
		tempmap.put(worldname, player.getLocation());
		playerdeathlocations.put(playerid, tempmap);
		saveDeathLocation(player);
	}

	
	/**
	 * Get death location from hashmap
	 * @param player
	 * @return
	 */
	public Location getDeathLocation(Player player) {
		
		// set playerid to player name
		String playerid = player.getName();
		
		// if use-uuid is enabled in config, set playerid to player uuid
		if (plugin.getConfig().getBoolean("use-uuid", true)) {
			playerid = player.getUniqueId().toString();
		}
		String worldname = player.getWorld().getName();
		return playerdeathlocations.get(playerid).get(worldname);
	}

	
	/**
	 * Save death location to data file
	 * @param player
	 */
	public void saveDeathLocation(Player player) {
		
		// set playerid to player name
		String playerid = player.getName();
		
		// if use-uuid is enabled in config, set playerid to player uuid
		if (plugin.getConfig().getBoolean("use-uuid", true)) {
			playerid = player.getUniqueId().toString();
		}

		// get player current world name
		String worldname = player.getWorld().getName();
		
		// get player last death location in current world from hashmap
		Location lastdeathloc = playerdeathlocations.get(playerid).get(worldname);

		// if player last death location in current world is null, do nothing and return
		if (lastdeathloc == null) {
			return;
		}
		
		// save location in data file
		deathlocationsfile.getConfig().set(playerid + "." + worldname, lastdeathloc.getWorld().getName());
		deathlocationsfile.getConfig().set(playerid + "." + worldname + ".x", lastdeathloc.getBlockX());
		deathlocationsfile.getConfig().set(playerid + "." + worldname + ".y", lastdeathloc.getBlockY());
		deathlocationsfile.getConfig().set(playerid + "." + worldname + ".z", lastdeathloc.getBlockZ());
		deathlocationsfile.saveConfig();
	}

	
	/**
	 * Load death location for player from data file
	 * @param player
	 */
	public void loadDeathLocation(Player player) {

		//set playerid to player name
		String playerid = player.getName();
		
		// if use-uuid is enabled in config, set playerid to player uuid
		if (plugin.getConfig().getBoolean("use-uuid", true)) {
			playerid = player.getUniqueId().toString();
		}
		
		// set worldname to player current world
		String worldname = player.getWorld().getName();

		// set lastdeathloc to player bed spawn location
		Location lastdeathloc = player.getBedSpawnLocation();
		
		// if player bedspawn is null, set lastdeathloc to world spawn location
		if (lastdeathloc == null) {
			lastdeathloc = player.getWorld().getSpawnLocation();
		}
		
		// if player last death location in current world is in data file, set it in lastdeathloc variable 
		if (deathlocationsfile.getConfig().contains(playerid + "." + worldname)) {
			lastdeathloc.setWorld(player.getWorld());
			lastdeathloc.setX(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".x"));
			lastdeathloc.setY(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".y"));
			lastdeathloc.setZ(deathlocationsfile.getConfig().getDouble(playerid + "." + worldname + ".z"));
		}
		
		HashMap<String, Location> tempmap = new HashMap<String, Location>();
		tempmap.put(worldname, lastdeathloc);
		playerdeathlocations.put(playerid, tempmap);
	}
}
