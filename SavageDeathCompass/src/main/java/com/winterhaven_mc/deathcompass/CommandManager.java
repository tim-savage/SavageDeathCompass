package com.winterhaven_mc.deathcompass;

import java.util.ArrayList;
//import java.util.List;



import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandManager implements CommandExecutor {
	
	private DeathCompassMain plugin;
	private ArrayList<String> enabledWorlds;
	
	private final ChatColor helpColor = ChatColor.YELLOW;
	private final ChatColor usageColor = ChatColor.GOLD;
	private final ChatColor errorColor = ChatColor.RED;

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public CommandManager(DeathCompassMain plugin) {
		
		this.plugin = plugin;
		plugin.getCommand("deathcompass").setExecutor(this);
		updateEnabledWorlds();
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
		
		String subcmd = "";
		
		if (args.length > 0) {
			subcmd = args[0];
		}
		else {
			subcmd = "status";
		}

		// reload command
		if (subcmd.equalsIgnoreCase("reload")) {
			return reloadCommand(sender,args);
		}
		
		// status command
		if (subcmd.equalsIgnoreCase("status")) {
			return statusCommand(sender,args);
		}
		
		// help command
		if (subcmd.equalsIgnoreCase("help")) {			
			return helpCommand(sender,args);
		}
		return false;
	}

	
	/**
	 * Reload command
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean reloadCommand(CommandSender sender, String args[]) {
		
		// if sender does not have reload permission, send error message
		if (!sender.hasPermission("deathcompass.reload")) {
			sender.sendMessage(errorColor + "You do not have permission for this command!");
			return true;
		}
	
		// reload config.yml
		plugin.reloadConfig();
	
		// update enabledWorlds field
		updateEnabledWorlds();
	
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
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean statusCommand(CommandSender sender, String args[]) {
		
		// if sender does not have status permission, send message
		if (!sender.hasPermission("deathcompass.status")) {
			sender.sendMessage(errorColor + "You do not have permission for this command!");
			return true;
		}
		
		sender.sendMessage((Object)ChatColor.DARK_AQUA + "[" + plugin.getName() +  "] " 
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + plugin.getDescription().getVersion());
		sender.sendMessage(ChatColor.GREEN + "Language: " + ChatColor.RESET + plugin.getConfig().getString("language"));
		sender.sendMessage(ChatColor.GREEN + "Storage type: " + ChatColor.RESET + plugin.dataStore.getName());
		if (plugin.dataStore.getType().equals(DataStoreType.YAML)) {
			sender.sendMessage(ChatColor.GREEN + "File Sync Interval: " + ChatColor.RESET + plugin.getConfig().getString("file-sync-interval"));
		}
		sender.sendMessage(ChatColor.GREEN + "Destroy On Drop: " + ChatColor.RESET + plugin.getConfig().getString("destroy-on-drop"));
		sender.sendMessage(ChatColor.GREEN + "Use UUID: " + ChatColor.RESET + plugin.getConfig().getString("use-uuid"));
		sender.sendMessage(ChatColor.GREEN + "Enabled Words: " + ChatColor.RESET + this.getEnabledWorlds().toString());
		return true;
	}

	
	/**
	 * Help command
	 * @param sender
	 * @param args
	 * @return
	 */
	boolean helpCommand(CommandSender sender, String args[]) {
		
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
	 * @param sender
	 * @param command
	 */
	void displayUsage(CommandSender sender, String command) {
	
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
	

	/**
	 * update enabledWorlds ArrayList field from config file settings
	 */
	public void updateEnabledWorlds() {
		
		// copy list of enabled worlds from config into enabledWorlds ArrayList field
		this.enabledWorlds = new ArrayList<String>(plugin.getConfig().getStringList("enabled-worlds"));
		
		// if enabledWorlds ArrayList is empty, add all worlds to ArrayList
		if (this.enabledWorlds.isEmpty()) {
			for (World world : plugin.getServer().getWorlds()) {
				enabledWorlds.add(world.getName());
			}
		}
		
		// remove each disabled world from enabled worlds field
		for (String disabledWorld : plugin.getConfig().getStringList("disabled-worlds")) {
			this.enabledWorlds.remove(disabledWorld);
		}
	}
	
	
	/**
	 * get list of enabled worlds
	 * @return ArrayList of String enabledWorlds
	 */
	public ArrayList<String> getEnabledWorlds() {
		return this.enabledWorlds;
	}
}
