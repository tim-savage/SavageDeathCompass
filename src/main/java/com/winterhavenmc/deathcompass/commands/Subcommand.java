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

import com.winterhavenmc.deathcompass.messages.MessageId;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Interface that defines a subcommand
 */
interface Subcommand {

	boolean onCommand(CommandSender sender, List<String> argsList);

	List<String> onTabComplete(final CommandSender sender, final Command command,
	                           final String alias, final String[] args);

	String getName();

	@SuppressWarnings("unused")
	String getUsage();

	void displayUsage(CommandSender sender);

	MessageId getDescription();

	String getPermissionNode();

}
