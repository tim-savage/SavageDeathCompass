package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.sounds.SoundId;
import com.winterhavenmc.deathcompass.storage.DataStore;

import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


/**
 * Reloads configuration settings of the plugin
 */
final class ReloadCommand extends SubcommandAbstract {

	private final PluginMain plugin;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "reload";
		this.usageString = "/deathcompass reload";
		this.description = MessageId.COMMAND_HELP_RELOAD;
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {
		// check for null parameter
		Objects.requireNonNull(sender);

		if (!sender.hasPermission("deathcompass.reload")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_RELOAD_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// copy default config from jar if it doesn't exist
		plugin.saveDefaultConfig();

		// reload config file
		plugin.reloadConfig();

		// update enabledWorlds list
		plugin.worldManager.reload();

		// reload messages
		plugin.messageBuilder.reload();

		// reload sounds
		plugin.soundConfig.reload();

		// reload datastore if changed
		DataStore.reload(plugin);

		// send success message
		plugin.messageBuilder.build(sender, MessageId.COMMAND_SUCCESS_RELOAD).send();

		// return true to prevent bukkit command help display
		return true;
	}

}
