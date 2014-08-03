package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.ConfigAccessor;
import com.winterhaven_mc.deathcompass.DeathCompassMain;

import java.io.File;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageManager {

	private final DeathCompassMain plugin;
	ConfigAccessor messages;

	public MessageManager(DeathCompassMain plugin) {
		this.plugin = plugin;

		// install localization files
		String[] localization_files = {"en-US", "es-ES", "de-DE"};
		
		installLocalizationFiles(localization_files);

		// get configured language
		String language = plugin.getConfig().getString("language","en-US");

		// check if localization file for configured language exists, if not then fallback to en-US
		if (!new File(plugin.getDataFolder() + "/language/" + language + ".yml").exists()) {
			plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
			language = "en-US";
		}

		// instantiate custom configuration manager
		messages = new ConfigAccessor(plugin, "language/" + language + ".yml");
	}

	public void sendPlayerMessage(Player player, String messageID) {

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
			player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
		}
	}

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

	private void installLocalizationFiles(String[] filelist) {
		for (String filename : filelist) {
			if (new File(plugin.getDataFolder() + "/language/" + filename + ".yml").exists()) {
				continue;
			}
			plugin.saveResource("language/" + filename + ".yml", false);
			plugin.getLogger().info("Installed localization files for " + filename + ".");
		}
	}

	public void reloadMessages() {
		messages.reloadConfig();
	}

	public String getItemName() {
		String itemname = messages.getConfig().getString("itemname","SpawnStar");
		return itemname;
	}

	public List<String> getItemLore() {
		List<String> itemlore = messages.getConfig().getStringList("itemlore");
		return itemlore;
	}   	

}

