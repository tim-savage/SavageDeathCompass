package com.winterhaven_mc.deathcompass.util;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.util.ConfigAccessor;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class MessageManager {

	// reference to main class
	private PluginMain plugin;

	// config accessor object for messages
	private ConfigAccessor messages;

	// the currently selected language
	private String language;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public MessageManager(PluginMain plugin) {

		this.plugin = plugin;

		// install any embedded resource localization files that are not already present
		installLocalizationFiles();

		// get configured language
		String language = plugin.getConfig().getString("language");

		// check if localization file for configured language exists, if not then fallback to en-US
		if (!new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}

		// instantiate custom configuration manager for language file
		messages = new ConfigAccessor(plugin, "language" + File.separator + language + ".yml");
	}


	/**
	 * Send a predefined message to a player
	 * @param sender the command sender for whom to display message
	 * @param messageID the message identifier
	 */
	public void sendPlayerMessage(CommandSender sender, String messageID) {

		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled")) {

			// set some string defaults in case sender is not a player
			String playerName = sender.getName();
			String playerNickname = playerName;
			String playerDisplayName = playerName;
			String worldName = "unknown";

			// if sender is a player then get nickname, display name and world name
			if (sender instanceof Player) {

				// get player for sender
				Player player = (Player) sender;

				// get player short name
				playerNickname = player.getPlayerListName();

				// get player display name
				playerDisplayName = player.getDisplayName();

				// get player world name
				worldName = plugin.worldManager.getWorldName(player.getWorld());
			}

			// get message string from localization file
			String message = messages.getConfig().getString("messages." + messageID + ".string");

			// strip color codes
			String itemName = messages.getConfig().getString("itemname").replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			playerName = playerName.replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			playerNickname = playerNickname.replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");

			// do variable substitutions
			if (message.contains("%")) {
				message = StringUtil.replace(message,"%itemname%", itemName);
				message = StringUtil.replace(message,"%playername%", playerName);
				message = StringUtil.replace(message,"%playerdisplayname%", playerDisplayName);
				message = StringUtil.replace(message,"%playernickname%", playerNickname);
				message = StringUtil.replace(message,"%worldname%", worldName);
			}
			
			// send message
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}


	/**
	 * Broadcast a predefined message
	 * @param sender the command sender whose name will be used in the message
	 * @param messageID the message identifier
	 */
	@SuppressWarnings("unused")
	public void broadcastMessage(Player sender, String messageID) {

		if (messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) {
			String message = messages.getConfig().getString("messages." + messageID + ".string");
			String itemname = messages.getConfig().getString("itemname").replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playername = sender.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playernickname = sender.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
			String playerdisplayname = sender.getDisplayName();
			String worldname = getWorldAlias(sender.getWorld().getName());

			if (message.contains("%")) {
				message = StringUtil.replace(message,"%itemname%", itemname);
				message = StringUtil.replace(message,"%playername%", playername);
				message = StringUtil.replace(message,"%playerdisplayname%", playerdisplayname);
				message = StringUtil.replace(message,"%playernickname%", playernickname);
				message = StringUtil.replace(message,"%worldname%", worldname);
			}

			this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes('&',message));
		}
	}


	@SuppressWarnings("unused")
	public String getLanguage() {
		return this.language;
	}


	/**
	 * Get item name from language specific messages file
	 * @return String itemname
	 */
	public String getItemName() {
		return messages.getConfig().getString("itemname");
	}


	/**
	 * Get item lore from language specific messages file
	 * @return List of strings, one string for each line of lore
	 */
	public List<String> getItemLore() {
		return messages.getConfig().getStringList("itemlore");
	}   	


	private String getWorldAlias(String worldName) {
		return plugin.worldManager.getWorldName(worldName);
	}


	public void reload() {

		// reinstall message files if necessary
		installLocalizationFiles();

		// get currently configured language
		String newLanguage = languageFileExists(plugin.getConfig().getString("language"));

		// if configured language has changed, instantiate new messages object
		if (!newLanguage.equals(this.language)) {
			this.messages = new ConfigAccessor(plugin, "language" + File.separator + newLanguage + ".yml");
			this.language = newLanguage;
			plugin.getLogger().info("New language " + this.language + " enabled.");
		}

		// reload language file
		messages.reloadConfig();
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
				if (name.startsWith("language" + '/') && name.endsWith(".yml")) {
					filelist.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : filelist) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}


	private String languageFileExists(String language) {

		// check if localization file for configured language exists, if not then fallback to en-US
		File languageFile = new File(plugin.getDataFolder() 
				+ File.separator + "language" 
				+ File.separator + language + ".yml");

		if (languageFile.exists()) {
			return language;
		}
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");
		return "en-US";
	}
}

