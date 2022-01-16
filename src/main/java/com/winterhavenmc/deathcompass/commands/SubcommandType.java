package com.winterhavenmc.deathcompass.commands;

import com.winterhavenmc.deathcompass.PluginMain;

public enum SubcommandType {

	HELP() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new HelpCommand(plugin, subcommandRegistry));
		}
	},

	RELOAD() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new ReloadCommand(plugin));
		}
	},

	STATUS() {
		@Override
		void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry) {
			subcommandRegistry.register(new StatusCommand(plugin));
		}
	};

	abstract void register(final PluginMain plugin, final SubcommandRegistry subcommandRegistry);

}
