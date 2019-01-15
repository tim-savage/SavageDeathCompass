package com.winterhaven_mc.deathcompass.util;

import com.winterhaven_mc.deathcompass.PluginMain;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;


public final class DeathCompass {

	// reference to plugin main class
	private final static PluginMain plugin = PluginMain.INSTANCE;

	// create itemTag string
	private final static String itemTag = plugin.messageManager.createHiddenString("DCv1");


	/**
	 * Private constructor to prevent class instantiation
	 */
	private DeathCompass() { throw new AssertionError(); }


	/**
	 * Create a DeathCompass item stack with custom display name and lore
	 *
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
	 *
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
	 *
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
