package com.winterhavenmc.deathcompass.listeners;

import com.winterhavenmc.deathcompass.PluginMain;
import com.winterhavenmc.deathcompass.sounds.SoundId;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;

import static com.winterhavenmc.deathcompass.messages.MessageId.ACTION_INVENTORY_DENY_TRANSFER;


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

		// get inventory action
		final InventoryAction action = event.getAction();

		// prevent DeathCompass shift-click transfer to container
		if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

			// check if current item is death compass
			if (plugin.deathCompassFactory.isDeathCompass(event.getCurrentItem())) {

				// if inventory type is in set, do nothing and return (allow transfer between player inventory and hot bar)
				if (SHIFT_CLICK_ALLOWED_TYPES.contains(event.getInventory().getType())) {
					return;
				}

				// cancel event
				event.setCancelled(true);

				// send player message
				plugin.messageBuilder.build(event.getWhoClicked(), ACTION_INVENTORY_DENY_TRANSFER).send();

				// play sound
				plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
			}
			return;
		}

		// prevent death compass transfer to container by swapping with other item
		if (action.equals(InventoryAction.SWAP_WITH_CURSOR)) {

			// check if cursor item or current item is death compass
			if (plugin.deathCompassFactory.isDeathCompass(event.getCursor())
					|| plugin.deathCompassFactory.isDeathCompass(event.getCurrentItem())) {

				// check if slot is in container inventory
				if (event.getRawSlot() < event.getInventory().getSize()) {

					// cancel event
					event.setCancelled(true);

					// send player message
					plugin.messageBuilder.build(event.getWhoClicked(), ACTION_INVENTORY_DENY_TRANSFER).send();

					// play sound
					plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
				}
			}
			return;
		}

		// prevent DeathCompass click transfer to container
		if (action.equals(InventoryAction.PLACE_ONE)
				|| action.equals(InventoryAction.PLACE_SOME)
				|| action.equals(InventoryAction.PLACE_ALL)) {

			// check if cursor item is a death compass
			if (plugin.deathCompassFactory.isDeathCompass(event.getCursor())) {

				// check if slot is in container inventory
				if (event.getRawSlot() < event.getInventory().getSize()) {

					// cancel event
					event.setCancelled(true);

					// send player message
					plugin.messageBuilder.build(event.getWhoClicked(), ACTION_INVENTORY_DENY_TRANSFER).send();

					// play sound
					plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
				}
			}
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
					event.setCancelled(true);

					// send player message
					plugin.messageBuilder.build(event.getWhoClicked(), ACTION_INVENTORY_DENY_TRANSFER).send();

					// play sound
					plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
					break;
				}
			}
		}
	}

}
