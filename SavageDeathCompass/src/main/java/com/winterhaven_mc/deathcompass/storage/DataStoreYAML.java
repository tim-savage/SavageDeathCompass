package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.util.ConfigAccessor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.util.*;


class DataStoreYAML extends DataStore {

	private final PluginMain plugin; // reference to main class
	private ConfigAccessor deathLocationFile;
	

	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
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
	public DeathRecord getRecord(UUID playerUUID, String worldName) {
		
		// if passed key is null return null record
		if (playerUUID == null 
				|| worldName == null 
				|| worldName.isEmpty()) {
			return null;
		}
		
		// convert playerUUID to string
		String playerUUIDString = playerUUID.toString();
		
		// if record is not in data file, return null
		if (!deathLocationFile.getConfig().getConfigurationSection(playerUUIDString).contains(worldName)) {
			return null;
		}

		// if world is invalid, return null
		if (plugin.getServer().getWorld(worldName) == null) {
			return null;
		}

		// get world
		World world = plugin.getServer().getWorld(worldName);

		// get location coordinates
		double x = deathLocationFile.getConfig().getDouble(playerUUIDString + "." + worldName + ".x",0);
		double y = deathLocationFile.getConfig().getDouble(playerUUIDString + "." + worldName + ".y",0);
		double z = deathLocationFile.getConfig().getDouble(playerUUIDString + "." + worldName + ".z",0);

		// create location
		Location location = new Location(world,x,y,z);

		// return destination object
		return new DeathRecord(playerUUID,location);
	}

	@Override
	public void putRecord(DeathRecord deathRecord) {
		
		// if destination is null do nothing and return
		if (deathRecord == null) {
			return;
		}
		
		// get player UUID as string
		String playerUUIDString = deathRecord.getPlayerUUID().toString();
		
		// get death location
		Location location = deathRecord.getLocation();
		
		// if world is not valid do nothing and return
		if (plugin.getServer().getWorld(location.getWorld().getName()) == null) {
			return;
		}
		
		// get world name
		String worldName = deathRecord.getLocation().getWorld().getName();
		
		// save location in data file
		deathLocationFile.getConfig().set(playerUUIDString + "." + worldName + ".x", location.getX());
		deathLocationFile.getConfig().set(playerUUIDString + "." + worldName + ".y", location.getY());
		deathLocationFile.getConfig().set(playerUUIDString + "." + worldName + ".z", location.getZ());
		
		// write in memory destination file to disk
		deathLocationFile.saveConfig();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	List<DeathRecord> getAllRecords() {
		
		List<DeathRecord> returnList = new ArrayList<DeathRecord>();
		SortedSet<String> playerIds = new TreeSet<String>(deathLocationFile.getConfig().getKeys(false));
		
		for (String playerId : playerIds) {
			
			// convert playerId to UUID
			UUID playerUUID = null;
			try {
				playerUUID = UUID.fromString(playerId);
			} catch (Exception e) {
				if (plugin.debug) {
					plugin.getLogger().warning("Could not convert playerUUID from string.");
				}
				
				// try retrieve player using playerId as name
				OfflinePlayer player = plugin.getServer().getOfflinePlayer(playerId);
				if (player != null) {
					playerUUID = player.getUniqueId();
				}
				
			}

			// if no match for player was found, skip to next record
			if (playerUUID == null) {
				continue;
			}
			
			DeathRecord record;
			
			Set<String> worldNames = new HashSet<String>(deathLocationFile
					.getConfig().getConfigurationSection(playerId).getKeys(false));

			for (String worldName : worldNames) {
				
				record = getRecord(playerUUID,worldName);
				if (record == null) {
					continue;
				}
				returnList.add(record);
			}
		}
		return returnList;
	}
	
	@Override
	DeathRecord deleteRecord(UUID playerUUID,String worldName) {
		
		// if key is null return null record
		if (playerUUID == null 
				|| worldName == null 
				|| worldName.isEmpty()) {
			return null;
		}
		
		// fetch death record, for return
		DeathRecord deathRecord = getRecord(playerUUID,worldName);

		// delete destination from storage
		deathLocationFile.getConfig().set(playerUUID.toString() + "." + worldName, null);
		
		// save in memory destination file to disk
		deathLocationFile.saveConfig();
		
		return deathRecord;
	}

	@Override
	public void close() {
		
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
	boolean delete() {

		Boolean deleteResult = false;
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		if (dataStoreFile.exists()) {
			deleteResult = dataStoreFile.delete();
		}
		return deleteResult;
	}
	
	@Override
	boolean exists() {
		
		// get path name to this data store file
		File dataStoreFile = new File(plugin.getDataFolder() + File.separator + this.getFilename());
		return dataStoreFile.exists();
	}
	
}
