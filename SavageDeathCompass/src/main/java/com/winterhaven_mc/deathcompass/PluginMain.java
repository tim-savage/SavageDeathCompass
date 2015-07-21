package com.winterhaven_mc.deathcompass;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 * 
 * @author      Tim Savage
 * @version		1.2
 */
public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;

	Boolean debug = this.getConfig().getBoolean("debug", false);

	MessageManager messageManager;
	CommandManager commandManager;
	DataStore dataStore;

	public void onEnable() {

		// static reference to main class
		instance = this;

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();
		
		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate command handler
		commandManager = new CommandManager(this);
		
		// instantiate datastore
		dataStore = DataStoreFactory.create();

		// instantiate event listener
		new PlayerEventListener(this);
	}

	public void onDisable() {
		dataStore.close();
	}

}

