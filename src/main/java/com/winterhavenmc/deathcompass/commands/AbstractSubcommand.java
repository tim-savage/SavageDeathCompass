package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.*;

abstract class AbstractSubcommand implements Subcommand {

	private String name;
	private String usageString;
	private MessageId description;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(final String name) {
		this.name = name;
	}

	@Override
	public String getUsage() {
		return usageString;
	}

	@Override
	public void displayUsage(CommandSender sender) {
		sender.sendMessage(usageString);
	}

	@Override
	public void setUsage(String usageString) {
		this.usageString = usageString;
	}

	@Override
	public MessageId getDescription() {
		return description;
	}

	@Override
	public void setDescription(final MessageId description) {
		this.description = description;
	}


	@Override
	public List<String> onTabComplete(final CommandSender sender, final Command command,
	                                  final String alias, final String[] args) {

		return Collections.emptyList();
	}

}
