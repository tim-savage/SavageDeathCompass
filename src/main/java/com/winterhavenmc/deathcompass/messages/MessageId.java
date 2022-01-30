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

package com.winterhavenmc.deathcompass.messages;

/**
 * Provides constant identifiers for configurable messages sent to players
 */
public enum MessageId {

	ACTION_PLAYER_RESPAWN,
	ACTION_ITEM_DESTROY,
	ACTION_INVENTORY_DENY_TRANSFER,

	COMMAND_FAIL_INVALID_COMMAND,
	COMMAND_FAIL_HELP_PERMISSION,
	COMMAND_FAIL_RELOAD_PERMISSION,
	COMMAND_FAIL_STATUS_PERMISSION,
	COMMAND_SUCCESS_RELOAD,

	COMMAND_HELP_INVALID,
	COMMAND_HELP_HELP,
	COMMAND_HELP_RELOAD,
	COMMAND_HELP_STATUS,
	COMMAND_HELP_USAGE,

}
