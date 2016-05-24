package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.commands.CommandManager;
import com.winterhaven_mc.deathcompass.listeners.PlayerEventListener;
import com.winterhaven_mc.deathcompass.storage.DataStore;
import com.winterhaven_mc.deathcompass.storage.DataStoreFactory;
import com.winterhaven_mc.deathcompass.util.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.winterhaven_mc.util.WorldManager;

/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 * 
 * @author      Tim Savage
 * @version		1.2
 */
public final class PluginMain extends JavaPlugin {

	public static PluginMain instance;

	public Boolean debug = this.getConfig().getBoolean("debug");

	public MessageManager messageManager;
	public WorldManager worldManager;
	public DataStore dataStore;

	public void onEnable() {

		// static reference to main class
		instance = this;

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();
		
		// instantiate world manager
		worldManager = new WorldManager(this);
		
		// instantiate message manager
		messageManager = new MessageManager(this);

		// instantiate datastore
		dataStore = DataStoreFactory.create();

		// instantiate command handler
		new CommandManager(this);

		// instantiate event listener
		new PlayerEventListener(this);
	}

	public void onDisable() {
		dataStore.close();
	}

}

