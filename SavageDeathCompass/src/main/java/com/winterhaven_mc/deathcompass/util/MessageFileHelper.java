package com.winterhaven_mc.deathcompass.util;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * Implements message manager for SpawnStar
 *
 * @author      Tim Savage
 * @version		1.0
 *
 */
final class MessageFileHelper {

	// reference to main class
	private final PluginMain plugin;

	// constant for language subdirectory name
	private final static String directoryName = "language";


	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	MessageFileHelper(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

	}


    /**
     * get language specified in config.yml
	 * @return IETF language string from config.yml
	 */
	private String getConfiguredLanguage() {
		return plugin.getConfig().getString("language");
	}


	/**
	 * Get the file name for the currently selected language
	 * @return current language file name as String
	 */
	private String getLanguageFileName(String language) {
		return  plugin.getDataFolder() + File.separator + directoryName + File.separator + language + ".yml";
	}


	/**
     * Load messages from file
	 * @return YamlConfiguration for specified language file
	 */
	final YamlConfiguration loadMessages() {

		// reinstall message files if necessary; this will not overwrite existing files
		installLocalizationFiles();

		// check that file exists for language
		String confirmedLanguage = languageFileExists(getConfiguredLanguage());

		// get file object for configured language file
		File languageFile = new File(getLanguageFileName(confirmedLanguage));

		// create new YamlConfiguration object
		YamlConfiguration newMessagesConfig = new YamlConfiguration();

		// try to load specified language file into new YamlConfiguration object
		try {
			newMessagesConfig.load(languageFile);
			plugin.getLogger().info("Language file " + confirmedLanguage + ".yml successfully loaded.");
		} catch (FileNotFoundException e) {
			plugin.getLogger().severe("Language file " + confirmedLanguage + ".yml does not exist.");
		} catch (IOException e) {
			plugin.getLogger().severe("Language file " + confirmedLanguage + ".yml could not be read.");
		} catch (InvalidConfigurationException e) {
			plugin.getLogger().severe("Language file " + confirmedLanguage + ".yml  is not valid yaml.");
		}

		return newMessagesConfig;
	}


	/**
	 * Install localization files from language directory in jar archive to language subdirectory
	 * of the plugin data directory. Any files with a .yml suffix that are stored as a resource
	 * within a /language subdirectory in the plugin jar archive will be copied to the /lanuguage
	 * subdirectory of the plugin data directory.
	 */
	private void installLocalizationFiles() {

		// initalize List of String to store matching filenames in jar
		List<String> fileList = new ArrayList<>();

		// get the absolute path to this plugin as URL
		final URL pluginURL = plugin.getServer().getPluginManager().getPlugin(plugin.getName()).getClass()
				.getProtectionDomain().getCodeSource().getLocation();

		// read files contained in jar, adding language/*.yml files to fileList
		ZipInputStream zip;
		try {
			zip = new ZipInputStream(pluginURL.openStream());
			while (true) {
				ZipEntry e = zip.getNextEntry();
				if (e == null) {
					break;
				}
				String name = e.getName();
				if (name.startsWith("language" + '/') && name.endsWith(".yml")) {
					fileList.add(name);
				}
			}
		} catch (IOException e1) {
			plugin.getLogger().warning("Could not read language files from jar.");
		}

		// iterate over list of language files and install from jar if not already present
		for (String filename : fileList) {
			// this check prevents a warning message when files are already installed
			if (new File(plugin.getDataFolder() + File.separator + filename).exists()) {
				continue;
			}
			plugin.saveResource(filename, false);
			plugin.getLogger().info("Installed localization file:  " + filename);
		}
	}

	/**
	 * Check if a file exists for the provided IETF language tag (ex: en-US)
	 * @param language the IETF language tag
	 * @return if file exists for language tag, return the language tag; else return the default tag (en-US)
	 */
	private String languageFileExists(final String language) {

		// get file object for passed language tag by adding prefixing for directory name and .yml suffix
		File languageFile = new File(getLanguageFileName(language));

		// if a language file exists for the language tag, return the language tag
		if (languageFile.exists()) {
			return language;
		}

		// output language file not found message to log
		plugin.getLogger().info("Language file " + language + ".yml does not exist. Defaulting to en-US.");

		// return default language tag (en-US)
		return "en-US";
	}


}
