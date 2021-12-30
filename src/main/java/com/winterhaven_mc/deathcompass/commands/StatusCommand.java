package com.winterhaven_mc.deathcompass.commands;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.sounds.SoundId;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;

import static com.winterhaven_mc.deathcompass.messages.MessageId.*;


public class StatusCommand extends AbstractSubcommand {

	private final PluginMain plugin;


	StatusCommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		setName("status");
		setUsage("/deathcompass status");
		setDescription(COMMAND_HELP_STATUS);
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		if (!sender.hasPermission("deathcompass.status")) {
			plugin.messageBuilder.build(sender, COMMAND_FAIL_STATUS_PERMISSION).send(plugin.languageHandler);
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + plugin.getDescription().getVersion());

		if (plugin.getConfig().getBoolean("debug")) {
			sender.sendMessage(ChatColor.GREEN + "Debug: "
					+ ChatColor.RED + plugin.getConfig().getString("debug"));
		}

		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));

		sender.sendMessage(ChatColor.GREEN + "Destroy On Drop: "
				+ ChatColor.RESET + plugin.getConfig().getString("destroy-on-drop"));

		sender.sendMessage(ChatColor.GREEN + "Set Compass Target Delay: "
				+ ChatColor.RESET + plugin.getConfig().getString("target-delay"));

		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString()
				+ ChatColor.RESET);
		return true;
	}

}