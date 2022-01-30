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

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Displays description and usage of a subcommand
 */
final class HelpCommand extends SubcommandAbstract {

	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry;


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 * @param subcommandRegistry reference to subcommand registry instance
	 */
	HelpCommand(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.name = "help";
		this.usageString = "/deathcompass help [command]";
		this.description = MessageId.COMMAND_HELP_HELP;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args) {

		List<String> returnList = new ArrayList<>();

		if (args.length == 2) {
			for (String subcommand : subcommandRegistry.getNames()) {
				if (sender.hasPermission("deathcompass." + subcommand)
						&& subcommand.startsWith(args[1].toLowerCase())
						&& !subcommand.equalsIgnoreCase("help")) {
					returnList.add(subcommand);
				}
			}
		}

		return returnList;
	}


	@Override
	public boolean onCommand(CommandSender sender, List<String> args) {

		// if command sender does not have permission to display help, output error message and return true
		if (!sender.hasPermission("deathcompass.help")) {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_HELP_PERMISSION).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_FAIL);
			return true;
		}

		// if no arguments, display usage for all commands
		if (args.size() == 0) {
			displayUsageAll(sender);
			return true;
		}

		// get subcommand name
		String subcommandName = args.get(0);
		displayHelp(sender, subcommandName);
		return true;
	}


	/**
	 * Display help message and usage for a command
	 *
	 * @param sender the command sender
	 * @param commandName the name of the command for which to show help and usage
	 */
	void displayHelp(final CommandSender sender, final String commandName) {

		// get subcommand from map by name
		Subcommand subcommand = subcommandRegistry.getCommand(commandName);

		// if subcommand found in map, display help message and usage
		if (subcommand != null) {
			plugin.messageBuilder.build(sender, subcommand.getDescription()).send();
			subcommand.displayUsage(sender);
		}

		// else display invalid command help message and usage for all commands
		else {
			plugin.messageBuilder.build(sender, MessageId.COMMAND_HELP_INVALID).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
			displayUsageAll(sender);
		}
	}


	/**
	 * Display usage message for all commands
	 *
	 * @param sender the command sender
	 */
	void displayUsageAll(CommandSender sender) {

		plugin.messageBuilder.build(sender, MessageId.COMMAND_HELP_USAGE).send();

		for (String subcommandName : subcommandRegistry.getNames()) {
			if (subcommandRegistry.getCommand(subcommandName) != null) {
				subcommandRegistry.getCommand(subcommandName).displayUsage(sender);
			}
		}
	}

}
