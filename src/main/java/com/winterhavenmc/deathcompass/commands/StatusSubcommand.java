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

package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.sounds.SoundId;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Objects;


/**
 * Displays configuration settings of the plugin
 */
final class StatusSubcommand extends AbstractSubcommand {

	private final PluginMain plugin;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	StatusSubcommand(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		this.name = "status";
		this.usageString = "/deathcompass status";
		this.description = MessageId.COMMAND_HELP_STATUS;
		this.permissionNode = "deathcompass.status";
	}


	@Override
	public boolean onCommand(final CommandSender sender, final List<String> args) {

		if (!sender.hasPermission(permissionNode)) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_STATUS_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		showVersion(sender);
		showDebug(sender);
		showLanguage(sender);
		showDestroyOnDrop(sender);
		showTargetDelay(sender);
		showEnabledWorlds(sender);
		return true;
	}


	private void showVersion(final CommandSender sender) {
		sender.sendMessage(ChatColor.DARK_AQUA + "[" + plugin.getName() + "] "
				+ ChatColor.AQUA + "Version: " + ChatColor.RESET + plugin.getDescription().getVersion());
	}


	private void showDebug(final CommandSender sender) {
		if (plugin.getConfig().getBoolean("debug")) {
			sender.sendMessage(ChatColor.GREEN + "Debug: "
					+ ChatColor.RED + plugin.getConfig().getString("debug"));
		}
	}


	private void showLanguage(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Language: "
				+ ChatColor.RESET + plugin.getConfig().getString("language"));
	}


	private void showDestroyOnDrop(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Destroy On Drop: "
				+ ChatColor.RESET + plugin.getConfig().getString("destroy-on-drop"));
	}


	private void showTargetDelay(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Set Compass Target Delay: "
				+ ChatColor.RESET + plugin.getConfig().getString("target-delay"));
	}


	private void showEnabledWorlds(final CommandSender sender) {
		sender.sendMessage(ChatColor.GREEN + "Enabled Words: "
				+ ChatColor.RESET + plugin.worldManager.getEnabledWorldNames().toString()
				+ ChatColor.RESET);
	}

}