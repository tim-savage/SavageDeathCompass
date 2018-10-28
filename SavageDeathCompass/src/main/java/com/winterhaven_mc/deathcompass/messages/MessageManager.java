package com.winterhaven_mc.deathcompass.messages;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.util.AbstractMessageManager;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;


public class MessageManager extends AbstractMessageManager {


	/**
	 * Class constructor
	 * @param plugin reference to plugin main class
	 */
	public MessageManager(PluginMain plugin) {

		// call super class constructor
		//noinspection unchecked
		super(plugin, MessageId.class);
	}


	@Override
	protected Map<String,String> getDefaultReplacements(CommandSender recipient) {

		Map<String,String> replacements = new HashMap<>();
		replacements.put("%PLAYER_NAME%",recipient.getName());
		replacements.put("%WORLD_NAME%",ChatColor.stripColor(getWorldName(recipient)));
		replacements.put("%ITEM_NAME%", ChatColor.stripColor(getItemName()));

		if (recipient instanceof Player) {
			replacements.put("%PLAYER_DISPLAYNAME%",((Player) recipient).getDisplayName());
			replacements.put("%PLAYER_NICKNAME%",((Player) recipient).getPlayerListName());
		}
		else {
			replacements.put("%PLAYER_DISPLAYNAME%",recipient.getName());
			replacements.put("%PLAYER_NICKNAME%",recipient.getName());
		}

		return replacements;
	}


	/**
	 * Send a predefined message to a player
	 * @param sender the command sender for whom to display message
	 * @param messageId the message identifier
	 */
	public void sendMessage(CommandSender sender, MessageId messageId) {

		//noinspection unchecked
		sendMessage(sender, messageId, getDefaultReplacements(sender));
	}

}
