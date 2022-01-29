package com.winterhavenmc.deathcompass.listeners;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.messages.MessageId;
import com.winterhavenmc.deathcompass.sounds.SoundId;

import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * Implements event handlers for inventory events
 */
public final class InventoryEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// set of inventory types to allow shift-click transfers from hot bar (item goes into player inventory)
	private final static Collection<InventoryType> SHIFT_CLICK_ALLOWED_TYPES = Set.of(
			InventoryType.BEACON,
			InventoryType.BREWING,
			InventoryType.CRAFTING,
			InventoryType.FURNACE,
			InventoryType.WORKBENCH );


	/**
	 * class constructor
	 *
	 * @param plugin reference to main class
	 */
	public InventoryEventListener(final PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Prevent hoppers from inserting DeathCompass items into containers
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryMoveItem(final InventoryMoveItemEvent event) {

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		// get item stack involved in event
		final ItemStack itemStack = event.getItem();

		// if item stack is death compass, cancel event
		if (plugin.deathCompassFactory.isDeathCompass(itemStack)) {
			event.setCancelled(true);
		}
	}


	/**
	 * Prevent placing items into containers if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		switch (event.getAction()) {

			case MOVE_TO_OTHER_INVENTORY:
				// check if current item is death compass
				if (plugin.deathCompassFactory.isDeathCompass(event.getCurrentItem())) {

					// if inventory type is in set, do nothing and return (allow transfer between player inventory and hot bar)
					if (SHIFT_CLICK_ALLOWED_TYPES.contains(event.getInventory().getType())) {
						return;
					}

					// cancel event and send player message
					cancelInventoryTransfer(event, event.getWhoClicked());
				}
				break;

			case SWAP_WITH_CURSOR:
				// check if cursor item or current item is death compass
				if (plugin.deathCompassFactory.isDeathCompass(event.getCursor())
						|| plugin.deathCompassFactory.isDeathCompass(event.getCurrentItem())) {

					// check if slot is in container inventory
					if (event.getRawSlot() < event.getInventory().getSize()) {
						cancelInventoryTransfer(event, event.getWhoClicked());
					}
				}
				break;

			case PLACE_ONE:
			case PLACE_SOME:
			case PLACE_ALL:
				// check if cursor item is a death compass
				if (plugin.deathCompassFactory.isDeathCompass(event.getCursor())) {

					// check if slot is in container inventory
					if (event.getRawSlot() < event.getInventory().getSize()) {
						cancelInventoryTransfer(event, event.getWhoClicked());
					}
				}
				break;
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryDrag(final InventoryDragEvent event) {

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		// if cursor item is a death compass
		if (plugin.deathCompassFactory.isDeathCompass(event.getOldCursor())) {

			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : event.getRawSlots()) {
				if (slot < event.getInventory().getSize()) {
					cancelInventoryTransfer(event, event.getWhoClicked());
					break;
				}
			}
		}
	}


	/**
	 * Cancel transfer of death compass in inventory, send player message and play sound
	 * @param event the event being cancelled
	 * @param player the player involved in the event
	 */
	private void cancelInventoryTransfer(final Cancellable event, final HumanEntity player) {
		event.setCancelled(true);
		plugin.messageBuilder.build(player, MessageId.ACTION_INVENTORY_DENY_TRANSFER).send();
		plugin.soundConfig.playSound(player, SoundId.INVENTORY_DENY_TRANSFER);
	}

}
