package com.winterhaven_mc.deathcompass.messages;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.util.SoundManager;
import com.winterhaven_mc.util.StringUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;


public class MessageManager {

	// reference to main class
	private PluginMain plugin;

	// configuration object for messages
	private YamlConfiguration messages;

	// message file helper
	private MessageFileHelper messageFileHelper;

	// sound manager
	private SoundManager soundManager;


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public MessageManager(PluginMain plugin) {

		this.plugin = plugin;

		// instantiate messageFileHelper
		this.messageFileHelper = new MessageFileHelper(plugin);

		// instantiate soundManager
		this.soundManager = new SoundManager(plugin);

		// load messages from file
		this.messages = messageFileHelper.loadMessages();

	}


	/**
	 * Send a predefined message to a player
	 * @param sender the command sender for whom to display message
	 * @param messageID the message identifier
	 */
	public void sendPlayerMessage(CommandSender sender, MessageId messageID) {

		if (messages.getBoolean("messages." + messageID.toString() + ".enabled")) {

			// set some string defaults in case sender is not a player
			String playerName = sender.getName();
			String playerNickname = playerName;
			String playerDisplayName = playerName;
			String worldName = "unknown";

			// if sender is a player then get nickname, display name and world name
			if (sender instanceof Player) {

				// get player for sender
				Player player = (Player) sender;

				// get player short name
				playerNickname = player.getPlayerListName();

				// get player display name
				playerDisplayName = player.getDisplayName();

				// get player world name
				worldName = plugin.worldManager.getWorldName(player.getWorld());
			}

			// get message string from localization file
			String message = messages.getString("messages." + messageID.toString() + ".string");

			// strip color codes
			String itemName = messages.getString("ITEM_NAME").replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			playerName = playerName.replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");
			playerNickname = playerNickname.replaceAll("[&§][0-9A-Za-zK-Ok-oRr]", "");

			// do variable substitutions
			if (message.contains("%")) {
				message = StringUtil.replace(message,"%itemname%", itemName);
				message = StringUtil.replace(message,"%playername%", playerName);
				message = StringUtil.replace(message,"%playerdisplayname%", playerDisplayName);
				message = StringUtil.replace(message,"%playernickname%", playerNickname);
				message = StringUtil.replace(message,"%worldname%", worldName);
			}
			
			// send message
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
		}
	}


	/**
	 * Play sound
	 * @param sender command sender (player) to play sound
	 * @param soundId unique identifier that refers to sound in sounds.yml
	 */
	public final void sendPlayerSound(final CommandSender sender, final SoundId soundId) {
		this.soundManager.playerSound(sender,soundId.toString());
	}


	/**
	 * Get item name from language specific messages file
	 * @return String itemname
	 */
	public String getItemName() {
		return messages.getString("ITEM_NAME");
	}


	/**
	 * Get item lore from language specific messages file
	 * @return List of strings, one string for each line of lore
	 */
	public List<String> getItemLore() {
		return messages.getStringList("ITEM_LORE");
	}


	/**
	 * Reload messages and sounds config files
	 */
	public void reload() {

		// reload messages
		this.messages = messageFileHelper.loadMessages();

		// reload sounds
		this.soundManager.reload();
	}

}

