package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.DeathCompassMain;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler
implements CommandExecutor {
    private DeathCompassMain plugin;

    public CommandHandler(DeathCompassMain plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int maxArgs = 1;
        if (args.length > maxArgs) {
            sender.sendMessage("Too many arguments!");
            return false;
        }
        if (args.length < 1 && sender.hasPermission("deathcompass.admin")) {
            String versionString = this.plugin.getDescription().getVersion();
            sender.sendMessage((Object)ChatColor.AQUA + "[DeathCompass] Version " + versionString);
            return true;
        }
        String subcmd = args[0];
        if (!cmd.getName().equalsIgnoreCase("deathcompass") || !subcmd.equalsIgnoreCase("reload") || !sender.hasPermission("deathcompass.reload")) return false;
        this.plugin.reloadConfig();
        sender.sendMessage("[DeathCompass] config reloaded!");
        return true;
    }
}

