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

package com.winterhavenmc.deathcompass.util;

import com.winterhavenmc.deathcompass.PluginMain;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;


public final class DeathCompassFactory {

	// reference to plugin main class
	private final PluginMain plugin;

	// create itemTag string
	private final NamespacedKey itemKey;


	/**
	 * Private constructor to prevent class instantiation
	 */
	public DeathCompassFactory(final PluginMain plugin) {
		this.plugin = plugin;
		this.itemKey = new NamespacedKey(plugin, "isItem");
	}


	/**
	 * Create a DeathCompass item stack with custom display name and lore
	 *
	 * @return ItemStack of DeathCompass
	 */
	public ItemStack createItem() {

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
	public boolean isDeathCompass(final ItemStack itemStack) {

		// if passed ItemStack is null, return false
		if (itemStack == null) {
			return false;
		}

		// if item stack is not a compass return false
		if (!itemStack.getType().equals(Material.COMPASS)) {
			return false;
		}

		// if item stack does not have metadata return false
		if (!itemStack.hasItemMeta()) {
			return false;
		}

		// if item stack does not have persistent data tag, return false
		//noinspection ConstantConditions
		return itemStack.getItemMeta().getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
	}


	/**
	 * Set ItemMetaData on ItemStack using custom display name and lore from language file.<br>
	 * Display name additionally has hidden itemTag to make it identifiable as a DeathCompass item.
	 *
	 * @param itemStack the ItemStack on which to set DeathCompass MetaData
	 */
	@SuppressWarnings("ConstantConditions")
	private void setMetaData(final ItemStack itemStack) {

		// retrieve item name from language file
		String itemName = plugin.messageBuilder.getItemName();

		// retrieve item lore from language file
		List<String> itemLore = plugin.messageBuilder.getItemLore();

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
		itemMeta.setDisplayName(ChatColor.RESET + itemName);

		// set item metadata Lore to value from config file
		itemMeta.setLore(itemLore);

		// set persistent data in item metadata
		itemMeta.getPersistentDataContainer().set(itemKey, PersistentDataType.BYTE, (byte) 1);

		// save new item metadata
		itemStack.setItemMeta(itemMeta);
	}

}
