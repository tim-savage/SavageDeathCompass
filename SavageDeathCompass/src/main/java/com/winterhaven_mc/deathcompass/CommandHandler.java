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
			sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("language"));
			sender.sendMessage(ChatColor.GREEN + "Storage Type: " + ChatColor.RESET + plugin.getConfig().getString("storage-type"));
			if (plugin.getConfig().getString("storage-type").equals("file")) {
				sender.sendMessage(ChatColor.GREEN + "File Sync Interval: " + ChatColor.RESET + plugin.getConfig().getString("file-sync-interval"));
			}
			sender.sendMessage(ChatColor.GREEN + "Destroy On Drop: " + ChatColor.RESET + plugin.getConfig().getString("destroy-on-drop"));
			sender.sendMessage(ChatColor.GREEN + "Use UUID: " + ChatColor.RESET + plugin.getConfig().getString("use-uuid"));
			sender.sendMessage(ChatColor.GREEN + "Enabled Words: " + ChatColor.RESET + plugin.getConfig().getString("enabled-worlds").toString());
			return true;
		}
		
		String subcmd = args[0];
		
		// reload command
		if (cmd.getName().equalsIgnoreCase("deathcompass") && 
				subcmd.equalsIgnoreCase("reload") &&
				sender.hasPermission("deathcompass.reload")) {

			// get current language setting
			String original_language = plugin.getConfig().getString("language", "en-US");
			
			// get current storage-type
			String original_storageType = plugin.getConfig().getString("storage-type","sqlite");

			// reload config.yml
			plugin.reloadConfig();
			String current_language = plugin.getConfig().getString("language", "en-US");
			
			// send reloaded message to command sender
			sender.sendMessage(ChatColor.AQUA + "[DeathCompass] config reloaded.");

			// if language setting has changed, instantiate new message manager with new language file
			if (!original_language.equals(current_language)) {
				plugin.messagemanager = new MessageManager(plugin);
				sender.sendMessage(ChatColor.AQUA + "Changed language to " + ChatColor.DARK_AQUA + current_language + ChatColor.AQUA + ".");
			}
			// otherwise just reload the messages file to pull in any changes
			else {
				plugin.messagemanager.reloadMessages();
			}
			
			// if storage-type has changed, copy records to new datastore
			String storageType = plugin.getConfig().getString("storage-type","sqlite");
			if (!original_storageType.equals(storageType)) {
				try {
					plugin.datastore.closeDb();
				} catch (Exception e) {
					plugin.getLogger().warning("Could not close datastore.");
				}
				if (storageType.equals("file")) {
					sender.sendMessage(ChatColor.AQUA + "Switched to " + ChatColor.DARK_AQUA + "file" + ChatColor.AQUA + " storage.");
					plugin.datastore = new DatastoreFile();
				}
				else {
					sender.sendMessage(ChatColor.AQUA + "Switched to " + ChatColor.DARK_AQUA + "sqlite" + ChatColor.AQUA + " storage.");
					plugin.datastore = new DatastoreSQLite();
				}
				// initialize new datastore
				try {
					plugin.datastore.initializeDb();
				} catch (Exception e) {
					plugin.getLogger().severe("Could not initialize new datastore on reload.");
				}
			}
			return true;
		}
		return false;
	}
}
