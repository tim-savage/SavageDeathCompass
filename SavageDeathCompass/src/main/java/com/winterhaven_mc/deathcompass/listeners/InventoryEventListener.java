package com.winterhaven_mc.deathcompass.listeners;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.sounds.SoundId;
import com.winterhaven_mc.deathcompass.messages.MessageId;
import com.winterhaven_mc.deathcompass.util.DeathCompass;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.ItemStack;

import java.util.*;


/**
 * Implements event handlers for inventory events
 */
public final class InventoryEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// set of inventory types to allow shift-click transfers from hotbar (item goes into player inventory)
	private final static Set<InventoryType> SHIFT_CLICK_ALLOWED_TYPES =
			Collections.unmodifiableSet(new HashSet<>(
					Arrays.asList(InventoryType.BEACON,
							InventoryType.BREWING,
							InventoryType.CRAFTING,
							InventoryType.FURNACE,
							InventoryType.WORKBENCH)));


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
	@EventHandler
	public final void onInventoryMoveItem(final InventoryMoveItemEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		// get itemstack involved in event
		final ItemStack itemStack = event.getItem();

		// if itemstack is death compass, cancel event
		if (DeathCompass.isDeathCompass(itemStack)) {
			event.setCancelled(true);
		}
	}


	/**
	 * Prevent placing items into containers if configured
	 *
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryClick(final InventoryClickEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		// get inventory action
		final InventoryAction action = event.getAction();

		// prevent DeathCompass shift-click transfer to container
		if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {

			// check if current item is death compass
			if (DeathCompass.isDeathCompass(event.getCurrentItem())) {

				// if inventory type is in set, do nothing and return (allow transfer between player inventory and hotbar)
				if (SHIFT_CLICK_ALLOWED_TYPES.contains(event.getInventory().getType())) {
					return;
				}

				// cancel event
				event.setCancelled(true);

				// send player message
				plugin.messageManager.sendMessage(event.getWhoClicked(),
						MessageId.ACTION_INVENTORY_DENY_TRANSFER);

				// play sound
				plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
			}
			return;
		}

		// prevent death compass transfer to container by swapping with other item
		if (action.equals(InventoryAction.SWAP_WITH_CURSOR)) {

			// check if cursor item or current item is death compass
			if (DeathCompass.isDeathCompass(event.getCursor())
					|| DeathCompass.isDeathCompass(event.getCurrentItem())) {

				// check if slot is in container inventory
				if (event.getRawSlot() < event.getInventory().getSize()) {

					// cancel event
					event.setCancelled(true);

					// send player message
					plugin.messageManager.sendMessage(event.getWhoClicked(),
							MessageId.ACTION_INVENTORY_DENY_TRANSFER);

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
			if (DeathCompass.isDeathCompass(event.getCursor())) {

				// check if slot is in container inventory
				if (event.getRawSlot() < event.getInventory().getSize()) {

					// cancel event
					event.setCancelled(true);

					// send player message
					plugin.messageManager.sendMessage(event.getWhoClicked(),
							MessageId.ACTION_INVENTORY_DENY_TRANSFER);

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
	@EventHandler
	public final void onInventoryDrag(final InventoryDragEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if prevent-storage is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("prevent-storage")) {
			return;
		}

		// if cursor item is a death compass
		if (DeathCompass.isDeathCompass(event.getOldCursor())) {

			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : event.getRawSlots()) {
				if (slot < event.getInventory().getSize()) {
					event.setCancelled(true);

					// send player message
					plugin.messageManager.sendMessage(event.getWhoClicked(), MessageId.ACTION_INVENTORY_DENY_TRANSFER);

					// play sound
					plugin.soundConfig.playSound(event.getWhoClicked(), SoundId.INVENTORY_DENY_TRANSFER);
					break;
				}
			}
		}
	}

}
