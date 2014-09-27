package com.winterhaven_mc.deathcompass;

import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 * 
 * @author      Tim Savage
 * @version		1.2
 */
public final class DeathCompassMain extends JavaPlugin {

	public static DeathCompassMain plugin;

	Boolean debug = this.getConfig().getBoolean("debug", false);
	Logger logger = Logger.getLogger("Minecraft");

	MessageManager messageManager;
	CommandHandler commandHandler;
	Datastore datastore;

	public void onEnable() {

		plugin = this;

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// instantiate message manager
		messageManager = new MessageManager(this);

		// register command executor
		commandHandler = new CommandHandler(this);
		
		// instantiate datastore
		datastore = initializeDatastore();

		// instantiate listener object
		new PlayerEventListener(this);
	}

	public void onDisable() {
		datastore.close();
	}

	
	/**
	 * Try to initialize configured datastore.
	 * If SQLite storage is selected, but fails, fall back to flat file.
	 * If flat file storage fails, disable plugin.
	 *
	 * @return datastore
	 */
	private Datastore initializeDatastore() {

		// if config has sqlite as storage option...
		if (getConfig().getString("storage-type","sqlite").equalsIgnoreCase("sqlite")) {

			// instantiate sqlite datastore
			datastore = new DatastoreSQLite();

			// and try to initialize.
			try {
				datastore.initialize();
			}
			catch (Exception e) {

				// can't init sqlite, so print log message, and try flat file instead
				getLogger().warning("Could not initialize SQLite datastore. Defaulting to flat file datastore.");
				datastore = new DatastoreFile();
				try {
					datastore.initialize();
				} catch (Exception e2) {

					// if flat file also fails, disable plugin.
					getLogger().warning("Could not initialize flat file datastore. Disabling SavageDeathCompass...");
					this.getPluginLoader().disablePlugin(this);
				}
			}
		}
		else {
			// instantiate file datastore
			datastore = new DatastoreFile();
			try {
				// try to init flat file
				datastore.initialize();
			}
			catch (Exception e2) {

				// if flat file init fails, disable plugin.
				getLogger().warning("Could not initialize flat file datastore. Disabling SavageDeathCompass...");
				this.getPluginLoader().disablePlugin(this);
			}
		}
		// return the datastore that was successfully initialized.
		return datastore;
	}

}

