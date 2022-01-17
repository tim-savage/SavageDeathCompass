package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.sounds.SoundId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.winterhavenmc.deathcompass.messages.MessageId.*;
import static com.winterhavenmc.deathcompass.sounds.SoundId.COMMAND_INVALID;


final class HelpCommand extends AbstractSubcommand {

	private final PluginMain plugin;
	private final SubcommandRegistry subcommandRegistry;


	HelpCommand(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
		this.plugin = Objects.requireNonNull(plugin);
		this.subcommandRegistry = Objects.requireNonNull(subcommandRegistry);
		this.setName("help");
		this.setUsage("/deathcompass help [command]");
		this.setDescription(COMMAND_HELP_HELP);
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
			plugin.messageBuilder.build(sender, COMMAND_FAIL_HELP_PERMISSION).send();
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
			plugin.messageBuilder.build(sender, COMMAND_HELP_INVALID).send();
			plugin.soundConfig.playSound(sender, COMMAND_INVALID);
			displayUsageAll(sender);
		}
	}


	/**
	 * Display usage message for all commands
	 * @param sender the command sender
	 */
	void displayUsageAll(CommandSender sender) {

		plugin.messageBuilder.build(sender, COMMAND_HELP_USAGE).send();

		for (String subcommandName : subcommandRegistry.getNames()) {
			if (subcommandRegistry.getCommand(subcommandName) != null) {
				subcommandRegistry.getCommand(subcommandName).displayUsage(sender);
			}
		}
	}

}
