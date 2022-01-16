package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;

public enum SubcommandType {

	RELOAD() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new ReloadCommand(plugin);
		}
	},

	STATUS() {
		@Override
		Subcommand create(final PluginMain plugin) {
			return new StatusCommand(plugin);
		}
	};

	abstract Subcommand create(final PluginMain plugin);

}
