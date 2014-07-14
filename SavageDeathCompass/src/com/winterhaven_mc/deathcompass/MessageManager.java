package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.ConfigAccessor;
import com.winterhaven_mc.deathcompass.DeathCompassMain;
import java.io.File;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class MessageManager {
    private final DeathCompassMain plugin;
    ConfigAccessor messages;

    public MessageManager(DeathCompassMain plugin) {
        this.plugin = plugin;
        String[] localization_files = new String[]{"de-DE", "en-US", "es-ES"};
        this.installLocalizationFiles(localization_files);
        String language = plugin.getConfig().getString("language", "en-US");
        if (!new File(plugin.getDataFolder() + "/language/" + language + "/messages.yml").exists()) {
            plugin.getLogger().info("Language file for " + language + " not found. Defaulting to en-US.");
            language = "en-US";
        }
        this.messages = new ConfigAccessor((JavaPlugin)plugin, "language/" + language + "/messages.yml");
    }

    public void sendPlayerMessage(Player player, String messageID) {
        if (!this.messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) return;
        String message = this.messages.getConfig().getString("messages." + messageID + ".string");
        String itemname = this.messages.getConfig().getString("itemname", "Death Compass").replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playerdisplayname = player.getDisplayName();
        String worldname = player.getWorld().getName();
        message = message.replaceAll("%itemname%", itemname);
        message = message.replaceAll("%playername%", playername);
        message = message.replaceAll("%playerdisplayname%", playerdisplayname);
        message = message.replaceAll("%playernickname%", playernickname);
        message = message.replaceAll("%worldname%", worldname);
        player.sendMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
    }

    public void broadcastMessage(Player player, String messageID) {
        if (!this.messages.getConfig().getBoolean("messages." + messageID + ".enabled", false)) return;
        String message = this.messages.getConfig().getString("messages." + messageID + ".string");
        String itemname = this.messages.getConfig().getString("itemname", "Death Compass").replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playername = player.getName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playernickname = player.getPlayerListName().replaceAll("&[0-9A-Za-zK-Ok-oRr]", "");
        String playerdisplayname = player.getDisplayName();
        String worldname = player.getWorld().getName();
        message = message.replaceAll("%itemname%", itemname);
        message = message.replaceAll("%playername%", playername);
        message = message.replaceAll("%playerdisplayname%", playerdisplayname);
        message = message.replaceAll("%playernickname%", playernickname);
        message = message.replaceAll("%worldname%", worldname);
        this.plugin.getServer().broadcastMessage(ChatColor.translateAlternateColorCodes((char)'&', (String)message));
    }

    private void installLocalizationFiles(String[] filelist) {
        for (String filename : filelist) {
            if (new File(this.plugin.getDataFolder() + "/language/" + filename + "/messages.yml").exists()) continue;
            this.plugin.saveResource("language/" + filename + "/messages.yml", false);
            this.plugin.getLogger().info("Installed localization files for " + filename + ".");
        }
    }

    public void reloadMessages() {
        this.messages.reloadConfig();
    }
}

