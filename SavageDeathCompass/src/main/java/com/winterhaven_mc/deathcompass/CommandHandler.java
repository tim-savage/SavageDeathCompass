package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.DeathCompassMain;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler
implements CommandExecutor {
	private DeathCompassMain plugin;

	public CommandHandler(DeathCompassMain plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		int maxArgs = 1;
		if (args.length > maxArgs) {
			sender.sendMessage("Too many arguments!");
			return false;
		}
		// if called with no arguments, output config settings
		if (args.length < 1 && sender.hasPermission("deathcompass.admin")) {
			sender.sendMessage((Object)ChatColor.AQUA + "[SavageDeathCompass] Version " + plugin.getDescription().getVersion());
			sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("Language"));
			sender.sendMessage(ChatColor.GREEN + "StorageType: " + ChatColor.RESET + plugin.getConfig().getString("StorageType"));
			sender.sendMessage(ChatColor.GREEN + "DestroyOnDrop: " + ChatColor.RESET + plugin.getConfig().getString("DestroyOnDrop"));
			sender.sendMessage(ChatColor.GREEN + "UseUUID: " + ChatColor.RESET + plugin.getConfig().getString("UseUUID"));
			sender.sendMessage(ChatColor.GREEN + "EnabledWords: " + ChatColor.RESET + plugin.getConfig().getString("EnabledWorlds").toString());
			return true;
		}
		
		String subcmd = args[0];
		
		// reload command
		if (cmd.getName().equalsIgnoreCase("deathcompass") && 
				subcmd.equalsIgnoreCase("reload") &&
				sender.hasPermission("deathcompass.reload")) {

			// get current language setting
			String original_language = plugin.getConfig().getString("Language", "en-US");
			
			// get current StorageType
			String original_storageType = plugin.getConfig().getString("StorageType","sqlite");

			// reload config.yml
			plugin.reloadConfig();

			// if language setting has changed, instantiate new message manager with new language file
			if (!original_language.equals(plugin.getConfig().getString("Language", "en-US"))) {
				plugin.messagemanager = new MessageManager(plugin);
			}
			else {
				plugin.messagemanager.reloadMessages();
			}

			// if StorageType has changed, copy records to new datastore
			String storageType = plugin.getConfig().getString("StorageType","sqlite");
			if (!original_storageType.equals(storageType)) {
				try {
					plugin.datastore.closeDb();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not close datastore.");
				}
				if (storageType.equals("file")) {
					plugin.getLogger().info("Switching to flat file storage...");
					plugin.datastore = new DatastoreFile();
				}
				else {
					plugin.getLogger().info("Switching to SQLite storage...");
					plugin.datastore = new DatastoreSQLite(); 
				}
				// initialize new datastore
				try {
					plugin.datastore.initializeDb();
				} catch (Exception e) {
					plugin.getLogger().severe("Could not initialize new datastore on reload.");
				}
			}
			// send reloaded message to command sender
			sender.sendMessage(ChatColor.AQUA + "[DeathCompass] config reloaded.");
			return true;
		}
		return false;
	}
}
