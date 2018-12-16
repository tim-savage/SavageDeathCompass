package com.winterhaven_mc.deathcompass.storage;

import com.winterhaven_mc.deathcompass.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;


public final class DeathCompass {

	private final UUID playerUUID;
	private final Location location;

	// reference to plugin main class
	private final static PluginMain plugin = PluginMain.instance;

	// create itemTag string
	private final static String itemTag = plugin.messageManager.createHiddenString("DCv1");


	/**
	 * Class constructor
	 * @param playerUUID the player UUID for the DeathCompass
	 * @param location the player death location for the DeathCompass
	 */
	DeathCompass(final UUID playerUUID, final Location location) {

		// set playerUUID
		this.playerUUID = playerUUID;

		// set player death location with defensive copy of passed location
		this.location = new Location(location.getWorld(),
				location.getX(),
				location.getY(),
				location.getZ(),
				location.getYaw(),
				location.getPitch());
	}
	

	/**
	 * Class constructor
	 * @param player the player whose UUID and location will be used to create the DeathCompass
	 */
	public DeathCompass(final Player player) {
		
		// set playerUUID
		this.playerUUID = player.getUniqueId();

		// set player death location with defensive copy of passed location
		this.location = new Location(player.getLocation().getWorld(),
				player.getLocation().getX(),
				player.getLocation().getY(),
				player.getLocation().getZ(),
				player.getLocation().getYaw(),
				player.getLocation().getPitch());
	}

	
	@SuppressWarnings("WeakerAccess")
	public UUID getPlayerUUID() {
		return playerUUID;
	}

	
	public Location getLocation() {
		return location;
	}


	/**
	 * Create a DeathCompass item stack with custom display name and lore
	 * @return ItemStack of DeathCompass
	 */
	public static ItemStack createItem() {

		// create compass item stack
		final ItemStack newItem = new ItemStack(Material.COMPASS, 1);

		// set item display name and lore
		setMetaData(newItem);

		// return new item
		return newItem;
	}


	/**
	 * Check if itemStack is a DeathCompass item
	 * @param itemStack the ItemStack to check
	 * @return {@code true} if itemStack is a DeathCompass item, {@code false} if not
	 */
	public static boolean isDeathCompass(final ItemStack itemStack) {

		// if passed ItemStack is null, return false
		if (itemStack == null) {
			return false;
		}

		// if item stack is not a compass return false
		if (!itemStack.getType().equals(Material.COMPASS)) {
			return false;
		}

		// if item stack does not have display name return false
		if (!itemStack.getItemMeta().hasDisplayName()) {
			return false;
		}

		// get item display name
		String itemDisplayName = itemStack.getItemMeta().getDisplayName();

		// check that name contains hidden token
		return itemDisplayName.startsWith(itemTag);
	}


	/**
	 * Set ItemMetaData on ItemStack using custom display name and lore from language file.<br>
	 * Display name additionally has hidden itemTag to make it identifiable as a DeathCompass item.
	 * @param itemStack the ItemStack on which to set DeathCompass MetaData
	 */
	private static void setMetaData(final ItemStack itemStack) {

		// retrieve item name from language file file
		String itemName = plugin.messageManager.getItemName();

		// retrieve item lore from language file file
		//noinspection unchecked
		List<String> itemLore = plugin.messageManager.getItemLore();

		// get item metadata object
		final ItemMeta itemMeta = itemStack.getItemMeta();

		// hide item stack attributes and enchants
		itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		itemMeta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		itemMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

		// set item metadata display name to value from config file
		itemMeta.setDisplayName(itemTag + ChatColor.RESET + itemName);

		// set item metadata Lore to value from config file
		itemMeta.setLore(itemLore);

		// save new item metadata
		itemStack.setItemMeta(itemMeta);
	}

}
