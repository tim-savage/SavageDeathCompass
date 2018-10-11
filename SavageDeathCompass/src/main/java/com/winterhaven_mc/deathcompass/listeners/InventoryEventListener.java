package com.winterhaven_mc.deathcompass.listeners;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.storage.DeathCompass;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;


public final class InventoryEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	
	/** class constructor
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
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryMoveItem(final InventoryMoveItemEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if event world is disabled, do nothing and return
		if (!plugin.worldManager.isEnabled(event.getDestination().getLocation().getWorld() )) {
			return;
		}

		// get itemstack involved in event
		final ItemStack itemStack = event.getItem();

		// if itemstack is death compass, cancel event
		if (DeathCompass.isDeathCompass(itemStack) && plugin.getConfig().getBoolean("no-chest")) {
			event.setCancelled(true);
		}
	}


	/**
	 * Prevent placing items in containers if configured
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryClick(final InventoryClickEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if player world is disabled, do nothing and return
		if (!plugin.worldManager.isEnabled(event.getWhoClicked().getWorld())) {
			return;
		}

		// if no-chest is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("no-chest")) {
			return;
		}

		final Inventory inventory = event.getInventory();
		final InventoryAction action = event.getAction();

		// prevent DeathCompass shift-click transfer to container
		if (DeathCompass.isDeathCompass(event.getCurrentItem())
				&& action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
			event.setCancelled(true);
			return;
		}

		// prevent DeathCompass click transfer to container
		if (DeathCompass.isDeathCompass(event.getCursor())
				&& action.equals(InventoryAction.PLACE_ONE)
				|| action.equals(InventoryAction.PLACE_ALL)
				|| action.equals(InventoryAction.PLACE_SOME)) {

			if (event.getRawSlot() < inventory.getSize()) {
				event.setCancelled(true);

				// send player message
				plugin.messageManager.sendPlayerMessage(event.getWhoClicked(),"chest-deny");
			}
		}
	}


	/**
	 * Prevent placing items in death chests if configured
	 * @param event the event being handled by this method
	 */
	@EventHandler
	public final void onInventoryDrag(final InventoryDragEvent event) {

		// if event is already cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if player world is disabled, do nothing and return
		if (!plugin.worldManager.isEnabled(event.getWhoClicked().getWorld())) {
			return;
		}

		// if no-chest is configured false, do nothing and return
		if (!plugin.getConfig().getBoolean("no-chest")) {
			return;
		}

		final Inventory inventory = event.getInventory();
		final ItemStack itemStack = event.getOldCursor();

		// if itemStack is a death compass
	    if (DeathCompass.isDeathCompass(itemStack)) {
	
			// iterate over dragged slots and if any are above max slot, cancel event
			for (int slot : event.getRawSlots()) {
				if (slot < inventory.getSize()) {
					event.setCancelled(true);

					// send player message
					plugin.messageManager.sendPlayerMessage(event.getWhoClicked(),"chest-deny");
					break;
				}
			}
	    }
	}

}
