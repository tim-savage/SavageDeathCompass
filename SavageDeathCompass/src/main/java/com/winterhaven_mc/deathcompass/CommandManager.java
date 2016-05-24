package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.storage.DataStoreFactory;
import com.winterhaven_mc.deathcompass.storage.DataStoreType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
	
	private PluginMain plugin;
	
	private final static ChatColor helpColor = ChatColor.YELLOW;
	private final static ChatColor usageColor = ChatColor.GOLD;
	private final static ChatColor errorColor = ChatColor.RED;

	
	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public CommandManager(PluginMain plugin) {
		
		this.plugin = plugin;
		plugin.getCommand("deathcompass").setExecutor(this);
	}

	/**
	 * Command handler
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		int maxArgs = 2;

		if (args.length > maxArgs) {
			sender.sendMessage(errorColor + "Too many arguments!");
			return false;
		}

		String subcommand;

		if (args.length > 0) {
			subcommand = args[0];
		}
		else {
			subcommand = "status";
		}

		// reload command
		if (subcommand.equalsIgnoreCase("reload")) {
			return reloadCommand(sender,args);
		}

		// status command
		if (subcommand.equalsIgnoreCase("status")) {
			return statusCommand(sender,args);
		}

		// help command
		if (subcommand.equalsIgnoreCase("help")) {
			return helpCommand(sender,args);
		}
		return false;
	}

	
	/**
	 * Reload command
	 * @param sender the command sender
	 * @param args the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage string
	 */
	private boolean reloadCommand(CommandSender sender, String args[]) {
		
		// if sender does not have reload permission, send error message
		if (!sender.hasPermission("deathcompass.reload")) {
			sender.sendMessage(errorColor + "You do not have permission for this command!");
			return true;
		}
	
		// reload config.yml
		plugin.reloadConfig();
	
		// update debug field
		plugin.debug = plugin.getConfig().getBoolean("debug");
		
		// update enabledWorlds field
		plugin.worldManager.reload();
	
		// reload messages
		plugin.messageManager.reload();
	
		// reload datastore
		DataStoreFactory.reload();
	
		// send reloaded message to command sender
		sender.sendMessage(ChatColor.AQUA + "[DeathCompass] config reloaded.");
		return true;
	}

	
	/**
	 * Status command
	 * @param sender the command sender
	 * @param args the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage string
	 */
	private boolean statusCommand(CommandSender sender, String args[]) {
		
		// if sender does not have status permission, send message
		if (!sender.hasPermission("deathcompass.status")) {
			sender.sendMessage(errorColor + "You do not have permission for this command!");
			return true;
		}
		
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() +  "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + plugin.getDescription().getVersion());
		
		sender.sendMessage(ChatColor.GREEN + "Language: " 
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Storage type: " 
				+ ChatColor.RESET + plugin.dataStore.getName());
		
		if (plugin.dataStore.getType().equals(DataStoreType.YAML)) {
			sender.sendMessage(ChatColor.GREEN + "File Sync Interval: " 
					+ ChatColor.RESET + plugin.getConfig().getString("file-sync-interval"));
		}
		
		sender.sendMessage(ChatColor.GREEN + "Destroy On Drop: " 
				+ ChatColor.RESET + plugin.getConfig().getString("destroy-on-drop"));
		
		sender.sendMessage(ChatColor.GREEN + "Enabled Words: " 
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString());
		return true;
	}

	
	/**
	 * Help command
	 * @param sender the command sender
	 * @param args the command arguments
	 * @return always returns {@code true}, to prevent display of bukkit usage string
	 */
	private boolean helpCommand(CommandSender sender, String args[]) {
		
		if (args.length < 1) {
			displayUsage(sender, "all");
			return true;
		}
		
		String command = "";
		if (args.length > 1) {
			command = args[1];
		}
		
		if (command.equalsIgnoreCase("status") 
				&& sender.hasPermission("deathcompass.status")) {
			sender.sendMessage(helpColor + "Display plugin configuration.");
		}
		if (command.equalsIgnoreCase("reload")
				&& sender.hasPermission("deathcompass.reload")) {
			sender.sendMessage(helpColor + "Reload plugin configuration.");
		}
		if (command.equalsIgnoreCase("help")) {
			sender.sendMessage(helpColor + "Display plugin help.");
		}
		displayUsage(sender,command);
		return true;
		
	}

	
	/**
	 * Display command usage
	 * @param sender the command sender
	 * @param command the command for which to display usage
	 */
	private void displayUsage(CommandSender sender, String command) {
	
		if (command.isEmpty()) {
			command = "all";
		}

		if ((command.equalsIgnoreCase("help") 
				|| command.equalsIgnoreCase("all"))) {
			sender.sendMessage(usageColor + "/deathcompass help [command]");
		}
		if ((command.equalsIgnoreCase("reload") 
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathcompass.reload")) {
			sender.sendMessage(usageColor + "/deathcompass reload");
		}
		if ((command.equalsIgnoreCase("status")	
				|| command.equalsIgnoreCase("all"))
				&& sender.hasPermission("deathcompass.status")) {
			sender.sendMessage(usageColor + "/deathcompass status");
		}
	}
	
}
