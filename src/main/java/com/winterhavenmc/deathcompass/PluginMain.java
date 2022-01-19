package com.winterhavenmc.deathcompass;

import com.winterhavenmc.deathcompass.commands.CommandManager;
import com.winterhavenmc.deathcompass.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.messages.Macro;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.storage.DataStore;
import com.winterhavenmc.deathcompass.util.DeathCompassFactory;

import com.winterhavenmc.util.messagebuilder.MessageBuilder;
import com.winterhavenmc.util.soundconfig.SoundConfiguration;
import com.winterhavenmc.util.soundconfig.YamlSoundConfiguration;
import com.winterhavenmc.util.worldmanager.WorldManager;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Bukkit plugin to give a compass on death
 * that points to players death location.
 *
 * @author Tim Savage
 */
public final class PluginMain extends JavaPlugin {

	public MessageBuilder<MessageId, Macro> messageBuilder;
	public SoundConfiguration soundConfig;
	public WorldManager worldManager;
	public DataStore dataStore;
	public DeathCompassFactory deathCompassFactory;


	@Override
	public void onEnable() {

		// bstats
		final int pluginId = 13925;
		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this, pluginId);

		// Save a copy of the default config.yml if file does not already exist
		saveDefaultConfig();

		// initialize message builder
		messageBuilder = new MessageBuilder<>(this);

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

		deathCompassFactory = new DeathCompassFactory(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}
