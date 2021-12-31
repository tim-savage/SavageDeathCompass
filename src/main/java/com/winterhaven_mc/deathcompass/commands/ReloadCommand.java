package com.winterhaven_mc.deathcompass.commands;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.sounds.SoundId;
import com.winterhaven_mc.deathcompass.storage.DataStore;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.deathcompass.messages.MessageId.*;

public class ReloadCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	ReloadCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		setName("reload");
		setUsage("/deathcompass reload");
		setDescription(COMMAND_HELP_RELOAD);
	}


	public boolean onCommand(final CommandSender sender, final List<String> args) {
		// check for null parameter
		Objects.requireNonNull(sender);

		if (!sender.hasPermission("deathcompass.reload")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_RELOAD_PERMISSION).send();
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
		plugin.messageBuilder.build(sender, COMMAND_SUCCESS_RELOAD).send();

		// return true to prevent bukkit command help display
		return true;
	}

}
