package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.commands.CommandManager;
import com.winterhaven_mc.deathcompass.listeners.InventoryEventListener;
import com.winterhaven_mc.deathcompass.listeners.PlayerEventListener;
import com.winterhaven_mc.deathcompass.messages.Macro;
import com.winterhaven_mc.deathcompass.messages.MessageId;
import com.winterhaven_mc.deathcompass.storage.DataStore;

import com.winterhaven_mc.util.WorldManager;
import com.winterhaven_mc.util.SoundConfiguration;
import com.winterhaven_mc.util.YamlSoundConfiguration;
import com.winterhavenmc.util.messagebuilder.LanguageHandler;
import com.winterhavenmc.util.messagebuilder.MessageBuilder;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin {

	public LanguageHandler languageHandler;
	public MessageBuilder<MessageId, Macro> messageBuilder;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;


	/**
	 * Class constructor for testing
	 */
	public PluginMain() {
		super();
	}


	/**
	 * Class constructor for testing
	 */
	@SuppressWarnings({"unused", "ProtectedMemberInFinalClass"})
	protected PluginMain(JavaPluginLoader loader, PluginDescriptionFile descriptionFile, File dataFolder, File file) {
		super(loader, descriptionFile, dataFolder, file);
	}


	@Override
	public void onEnable() {

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// initialize language manager
		languageHandler = new LanguageHandler(this);
		messageBuilder = new MessageBuilder<>();

		// instantiate sound config
		soundConfig = new YamlSoundConfiguration(this);

		// instantiate world manager
		worldManager = new WorldManager(this);

		// instantiate datastore
		dataStore = DataStore.connect(this);

		// instantiate command handler
		new CommandManager(this);

		// instantiate player event listener
		new PlayerEventListener(this);

		// instantiate inventory event listener
		new InventoryEventListener(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}
