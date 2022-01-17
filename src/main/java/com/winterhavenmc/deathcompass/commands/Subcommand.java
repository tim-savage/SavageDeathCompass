package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

interface Subcommand {

	boolean onCommand(CommandSender sender, List<String> argsList);

	List<String> onTabComplete(final CommandSender sender, final Command command,
	                           final String alias, final String[] args);

	String getName();

	void setName(final String name);

	List<String> getAliases();

	void setAliases(List<String> aliases);

	void addAlias(String alias);

	String getUsage();

	void setUsage(String usageString);

	void displayUsage(CommandSender sender);

	MessageId getDescription();

	void setDescription(MessageId messageId);

}
