package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;

import com.winterhavenmc.deathcompass.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import javax.annotation.Nonnull;
import java.util.*;

import static com.winterhavenmc.deathcompass.sounds.SoundId.*;


/**
 * A class that implements player commands for the plugin
 */
public final class CommandManager implements CommandExecutor, TabCompleter {

	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry = new SubcommandRegistry();


	public CommandManager(final PluginMain plugin) {
		this.plugin = Objects.requireNonNull(plugin);
		Objects.requireNonNull(plugin.getCommand("deathcompass")).setExecutor(this);

		for (SubcommandType subcommandType : SubcommandType.values()) {
			subcommandType.register(plugin, subcommandRegistry);
		}
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
			Subcommand subcommand = subcommandRegistry.getCommand(args[0]);

			// if no subcommand returned from map, return empty list
			if (subcommand == null) {
				return Collections.emptyList();
			}

			// return subcommand tab completer output
			return subcommand.onTabComplete(sender, command, alias, args);
		}

		// return list of subcommands for which sender has permission
		return matchingCommands(sender, args[0]);
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
		Subcommand subcommand = subcommandRegistry.getCommand(subcommandName);

		// if subcommand is null, get help command from map
		if (subcommand == null) {
			subcommand = subcommandRegistry.getCommand("help");
			plugin.messageBuilder.build(sender, MessageId.COMMAND_FAIL_INVALID_COMMAND).send();
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
		}

		// execute subcommand
		return subcommand.onCommand(sender, argsList);
	}


	/**
	 * Get matching list of subcommands for which sender has permission
	 * @param sender the command sender
	 * @param matchString the string prefix to match against command names
	 * @return List of String - command names that match prefix and sender has permission
	 */
	private List<String> matchingCommands(CommandSender sender, String matchString) {

		List<String> returnList = new ArrayList<>();

		for (String subcommand : subcommandRegistry.getNames()) {
			if (sender.hasPermission("deathcompass." + subcommand)
					&& subcommand.startsWith(matchString.toLowerCase())) {
				returnList.add(subcommand);
			}
		}
		return returnList;
	}

}
