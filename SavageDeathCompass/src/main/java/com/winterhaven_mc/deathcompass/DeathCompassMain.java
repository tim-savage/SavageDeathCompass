package com.winterhaven_mc.deathcompass;

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
	
	public static DeathCompassMain plugin;

	Boolean debug = this.getConfig().getBoolean("debug", false);
	Logger logger= Logger.getLogger("Minecraft");

	public MessageManager messagemanager;

	Datastore datastore;

	public void onEnable() {
		
		plugin = this;

		// register command executor
		getCommand("deathcompass").setExecutor((CommandExecutor)new CommandHandler(this));

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// instantiate listener object
		new PlayerEventListener(this);
		
		// instantiate death location manager
		//deathlocations = new DeathLocationManager(this);
			
		// instantiate datastore
		datastore = initDatastore();
		
		/*// OLD METHOD. TRYING NEW METHOD ABOVE.
		String storageType = getConfig().getString("StorageType","file");
		
		if (storageType != null && storageType.equalsIgnoreCase("sqlite")) {
			datastore = new DatastoreSQLite();
		}
		else {
			datastore = new DatastoreFile();
		}
		
		try {
			datastore.initializeDb();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		// instantiate message manager
		messagemanager = new MessageManager(this);
	}
	
	public void onDisable() {
		
		try {
			datastore.closeDb();
		} catch (Exception e) {
			getLogger().severe("Could not close datastore.");
			e.printStackTrace();
		}
	}

	/**
	 * Try to initialize configured datastore.
	 * If SQLite storage is selected, but fails, fall back to flat file.
	 * If flat file storage fails, disable plugin.
	 *
	 * @return datastore
	 */
	private Datastore initDatastore() {
		
		// if config has sqlite as storage option...
		if (getConfig().getString("StorageType","file").equalsIgnoreCase("sqlite")) {

			// instantiate sqlite datastore
			datastore = new DatastoreSQLite();
			
			// and try to initialize.
			try {
				datastore.initializeDb();
			} catch (Exception e) {
				
				// can't init sqlite, so print log message, and try flat file instead
				getLogger().warning("Could not initialize SQLite datastore. Defaulting to flat file datastore.");
				datastore = new DatastoreFile();
				try {
					datastore.initializeDb();
				} catch (Exception e2) {

					// if flat file also fails, disable plugin.
					getLogger().warning("Could not initialize file datastore. Disabling SavageDeathCompass...");
					this.getPluginLoader().disablePlugin(this);
				}
			}
		}
		else {
			// instantiate file datastore
			datastore = new DatastoreFile();
			try {
				// try to init flat file
				datastore.initializeDb();
			} catch (Exception e2) {
				
				// if flat file init fails, disable plugin.
				getLogger().warning("Could not initialize file datastore. Disabling SavageDeathCompass...");
				this.getPluginLoader().disablePlugin(this);
			}
		}
		// return the datastore that was successfully initialized.
		return datastore;
	}

}

