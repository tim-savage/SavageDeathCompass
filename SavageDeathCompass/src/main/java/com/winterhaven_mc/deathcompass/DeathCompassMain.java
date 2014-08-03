package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.CommandHandler;
import com.winterhaven_mc.deathcompass.DeathLocationManager;
import com.winterhaven_mc.deathcompass.MessageManager;
import java.util.logging.Logger;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 * 
 * @author      Tim Savage
 * @version		1.0
 */
public final class DeathCompassMain extends JavaPlugin {

	Boolean debug = this.getConfig().getBoolean("debug", false);
	Logger logger= Logger.getLogger("Minecraft");

	public DeathLocationManager deathlocations;
	public MessageManager messagemanager;

	public void onEnable() {

		// register command executor
		getCommand("deathcompass").setExecutor((CommandExecutor)new CommandHandler(this));

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// instantiate listener object
		new PlayerEventListener(this);
		
		// instantiate death location manager
		deathlocations = new DeathLocationManager(this);

		// instantiate message manager
		messagemanager = new MessageManager(this);
	}

}

