package com.winterhaven_mc.deathcompass;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bukkit.Location;
import org.bukkit.World;

public class DataStoreYAML extends DataStore {

	private final PluginMain plugin; // reference to main class
	private ConfigAccessor deathLocationFile;
	

	/**
	 * Class constructor
	 * @param plugin
	 */
	DataStoreYAML(PluginMain plugin) {
		
		// reference to main class
		this.plugin = plugin;
		
		// set datastore type
		this.type = DataStoreType.YAML;
		
		// set filename
		this.filename = "deathlocations.yml";
	}
	
	
	@Override
	void initialize() throws IllegalStateException, IllegalArgumentException {
		
		// if data store is already initialized, do nothing and return
		if (this.isInitialized()) {
			if (plugin.debug) {
				plugin.getLogger().info("yaml datastore already initialized.");
			}
			return;
		}

		// create new ConfigAccessor object
		deathLocationFile = new ConfigAccessor(plugin, filename);
		
		// copy embedded default file if necessary
		deathLocationFile.saveDefaultConfig();
		
		// set initialized true
		setInitialized(true);
		if (plugin.debug) {
			plugin.getLogger().info("yaml datastore initialized.");
		}
	}

	@Override
	DeathRecord getRecord(String playerId, String worldName) {
		
		// if passed key is null return null record
		if (playerId == null 
				|| worldName == null 
				|| playerId.isEmpty() 
				|| worldName.isEmpty()) {
			return null;
		}
		
		// if record is not in data file, return null
		if (!deathLocationFile.getConfig().getConfigurationSection(playerId).contains(worldName)) {
			return null;
		}

		// if world is invalid, return null
		if (plugin.getServer().getWorld(worldName) == null) {
			return null;
		}

		// get world
		World world = plugin.getServer().getWorld(worldName);

		// get location coordinates
		double x = deathLocationFile.getConfig().getDouble(playerId + "." + worldName + ".x",0);
		double y = deathLocationFile.getConfig().getDouble(playerId + "." + worldName + ".y",0);
		double z = deathLocationFile.getConfig().getDouble(playerId + "." + worldName + ".z",0);

		// create location
		Location location = new Location(world,x,y,z);

		// return destination object
		return new DeathRecord(playerId,location);
	}

	@Override
	void putRecord(DeathRecord deathRecord) {
		
		// if destination is null do nothing and return
		if (deathRecord == null) {
			return;
		}
		
		// get player id
		String playerId = deathRecord.getPlayerId();
		
		// get death location
		Location location = deathRecord.getLocation();
		
		// if world is not valid do nothing and return
		if (plugin.getServer().getWorld(location.getWorld().getName()) == null) {
			return;
		}
		
		// get world name
		String worldName = deathRecord.getLocation().getWorld().getName();
		
		// save location in data file
		deathLocationFile.getConfig().set(playerId + "." + worldName + ".x", location.getX());
		deathLocationFile.getConfig().set(playerId + "." + worldName + ".y", location.getY());
		deathLocationFile.getConfig().set(playerId + "." + worldName + ".z", location.getZ());
		
		// write in memory destination file to disk
		deathLocationFile.saveConfig();
	}
	
	@Override
	List<DeathRecord> getAllRecords() {
		
		List<DeathRecord> returnList = new ArrayList<DeathRecord>();
		SortedSet<String> playerIds = new TreeSet<String>(deathLocationFile.getConfig().getKeys(false));
		for (String playerId : playerIds) {
			
			DeathRecord record = null;
			
			HashSet<String> worldNames = new HashSet<String>(deathLocationFile
					.getConfig().getConfigurationSection(playerId).getKeys(false));

			for (String worldName : worldNames) {
				
				record = getRecord(playerId,worldName);
				if (record == null) {
					continue;
				}
			}
			returnList.add(record);
		}	
		return returnList;
	}
	
	@Override
	DeathRecord deleteRecord(String playerId,String worldName) {
		
		// if key is null return null record
		if (playerId == null 
				|| worldName == null 
				|| playerId.isEmpty() 
				|| worldName.isEmpty()) {
			return null;
		}
		
		// fetch death record, for return
		DeathRecord deathRecord = getRecord(playerId,worldName);

		// delete destination from storage
		deathLocationFile.getConfig().set(playerId + "." + worldName, null);
		
		// save in memory destination file to disk
		deathLocationFile.saveConfig();
		
		return deathRecord;
	}

	@Override
	void close() {
		
		// save data to file
		deathLocationFile.saveConfig();
		
		// set initialized to false
		setInitialized(false);
	}
	
	@Override
	void save() {
		deathLocationFile.saveConfig();
	}
	
	@Override
	void delete() {
		
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			dataStoreFile.delete();
		}
	}
	
	@Override
	boolean exists() {
		
		// get path name to this data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();

	}
	
}
