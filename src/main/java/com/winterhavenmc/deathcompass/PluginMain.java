/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathcompass;

import com.winterhavenmc.deathcompass.commands.CommandManager;
import com.winterhavenmc.deathcompass.listeners.InventoryEventListener;
import com.winterhavenmc.deathcompass.listeners.PlayerEventListener;
import com.winterhavenmc.deathcompass.messages.Macro;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.storage.DataStore;
import com.winterhavenmc.deathcompass.util.DeathCompassFactory;

import com.winterhavenmc.deathcompass.util.MetricsHandler;
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

		// instantiate death compass factory
		deathCompassFactory = new DeathCompassFactory(this);

		// instantiate metrics handler
		new MetricsHandler(this);
	}


	@Override
	public void onDisable() {
		dataStore.close();
	}

}
