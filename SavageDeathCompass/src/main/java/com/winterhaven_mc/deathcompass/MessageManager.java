package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.ConfigAccessor;
import com.winterhaven_mc.deathcompass.DeathCompassMain;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageManager {

	private final DeathCompassMain plugin;
	ConfigAccessor messages;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public MessageManager(DeathCompassMain plugin) {
		this.plugin = plugin;

		// install any embedded resource localization files that are not already present
		installLocalizationFiles();

		// get configured language
		String language = plugin.getConfig().getString("language","en-US");

		// check if localization file for configured language exists, if not then fallback to en-US
		if (!new File(plugin.getDataFolder() + File.separator + "language" + File.separator + language + ".yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}

		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language" + File.separator + language + ".yml");
	}

	
	/**
	 * Send a predefined message to a player
	 * @param player
	 * @param messageID
	 */
	public void sendPlayerMessage(Player player, String messageID) {

		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");
			String itemname = messages.getConfig().getString("itemname", "Death Compass").replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			String playername = player.getName().replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			String playernickname = player.getPlayerListName().replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			String playerdisplayname = player.getDisplayName();
			String worldname = player.getWorld().getName();
			message = message.replaceAll("%itemname%", itemname);
			message = message.replaceAll("%playername%", playername);
			message = message.replaceAll("%playerdisplayname%", playerdisplayname);
			message = message.replaceAll("%playernickname%", playernickname);
			message = message.replaceAll("%worldname%", worldname);
			player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
		}
	}

	
	/**
	 * Broadcast a predefined message
	 * @param player
	 * @param messageID
	 */
	public void broadcastMessage(Player player, String messageID) {
		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");
			String itemname = messages.getConfig().getString("itemname", "Death Compass").replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerdisplayname = player.getDisplayName();
			String worldname = player.getWorld().getName();
			message = message.replaceAll("%itemname%", itemname);
			message = message.replaceAll("%playername%", playername);
			message = message.replaceAll("%playerdisplayname%", playerdisplayname);
			message = message.replaceAll("%playernickname%", playernickname);
			message = message.replaceAll("%worldname%", worldname);
			this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
		}
	}

	
	/**
	 * Install localization files from <em>language</em> directory in jar 
	 */
	private void installLocalizationFiles() {

		List<String> filelist = new ArrayList<String>();

		// get the absolute path to this plugin as URL
		URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass().getProtectionDomain().getCodeSource().getLocation();

		// read files contained in jar, adding language/*.yml files to list
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith("language" + File.separator) && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}

	
	/**
	 * Reload messages file
	 */
	public void reloadMessages() {
		messages.reloadConfig();
	}

	
	/**
	 * Get item name from language specific messages file
	 * @return String itemname
	 */
	public String getItemName() {
		String itemname = messages.getConfig().getString("itemname","SpawnStar");
		return itemname;
	}

	
	/**
	 * Get item lore from language specific messages file
	 * @return List of strings, one string for each line of lore
	 */
	public List<String> getItemLore() {
		List<String> itemlore = messages.getConfig().getStringList("itemlore");
		return itemlore;
	}   	

}

