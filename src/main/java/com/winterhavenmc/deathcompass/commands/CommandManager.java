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
import org.bukkit.command.TabExecutor;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;


/**
 * A class that implements subcommands for the plugin
 */
public final class CommandManager implements TabExecutor {

	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();


	/**
	 * Class constructor
	 *
	 * @param plugin reference to plugin main class
	 */
	public CommandManager(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		Objects.requireNonNull(plugin.getCommand("deathcompass")).setExecutor(this);

		// register subcommands
		for (SubcommandType subcommandType : SubcommandType.values()) {
			subcommandRegistry.register(subcommandType.create(plugin));
		}

		// register help subcommand
		subcommandRegistry.register(new HelpSubcommand(plugin, subcommandRegistry));
	}


	/**
	 * Tab completer for DeathChest
	 *
	 * @param sender  the command sender
	 * @param command the command typed
	 * @param alias   alias for the command
	 * @param args    additional command arguments
	 * @return List of String - the possible matching values for tab completion
	 */
	@Override
	public List<String> onTabComplete(final @Nonnull CommandSender sender, final @Nonnull Command command,
	                                  final @Nonnull String alias, final String[] args) {

		// if more than one argument, use tab completer of subcommand
		if (args.length > 1) {

			// get subcommand from map
			Optional<Subcommand> subcommand = subcommandRegistry.getSubcommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (subcommand.isEmpty()) {
				return Collections.emptyList();
			}

			// return subcommand tab completer output
			return subcommand.get().onTabComplete(sender, command, alias, args);
		}

		// return list of matching subcommands for which sender has permission
		return getMatchingSubcommandNames(sender, args[0]);
	}


	/**
	 * Command handler for DeathChest
	 *
	 * @param sender   the command sender
	 * @param command  the command typed
	 * @param label    the command label
	 * @param args     Array of String - command arguments
	 * @return boolean - always returns {@code true}, to suppress bukkit builtin help message
	 */
	@Override
	public boolean onCommand(final @Nonnull CommandSender sender,
	                               final @Nonnull Command command,
	                               final @Nonnull String label,
	                               final String[] args) {

		// convert args array to list
		List<String> argsList = new ArrayList<>(Arrays.asList(args));

		String subcommandName;

		// get subcommand, remove from front of list
		if (argsList.size() > 0) {
			subcommandName = argsList.remove(0);
		}

		// if no arguments, set command to help
		else {
			subcommandName = "help";
		}

		// get subcommand from map by name
		Optional<Subcommand> optionalSubcommand = subcommandRegistry.getSubcommand(subcommandName);

		// if subcommand is empty, get help command from map
		if (optionalSubcommand.isEmpty()) {
			optionalSubcommand = subcommandRegistry.getSubcommand("help");
			plugin.messageBuilder.compose(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND).send();
			plugin.soundConfig.playSound(sender, SoundId.COMMAND_INVALID);
		}

		// execute subcommand
		optionalSubcommand.ifPresent( subcommand -> subcommand.onCommand(sender, argsList) );

		return true;
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 *
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> getMatchingSubcommandNames(final CommandSender sender, final String matchString) {

		return subcommandRegistry.getKeys().stream()
				.map(subcommandRegistry::getSubcommand)
				.filter(Optional::isPresent)
				.filter(subcommand -> sender.hasPermission(subcommand.get().getPermissionNode()))
				.map(subcommand -> subcommand.get().getName())
				.filter(name -> name.toLowerCase().startsWith(matchString.toLowerCase()))
				.collect(Collectors.toList());
	}

}
