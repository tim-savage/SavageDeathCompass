package com.winterhaven_mc.deathcompass.listeners;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.sounds.SoundId;
import com.winterhaven_mc.deathcompass.storage.DeathCompass;
import com.winterhaven_mc.deathcompass.messages.MessageId;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;


public class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// player death respawn hash set, used to prevent giving compass on non-death respawn events
	private Set<UUID> deathTriggeredRespawn = new HashSet<>();

	
	/**
	 * Class constructor
	 * @param plugin reference to main class
	 */
	public PlayerEventListener(PluginMain plugin) {

		// set reference to main class
		this.plugin = plugin;

		// register event handlers in this class
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Player death event handler
	 * @param event the event handled by this method
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) {
		
		// if destroy-on-drop is enabled in configuration, remove any death compasses from player drops on death
		if (plugin.getConfig().getBoolean("destroy-on-drop")) {

			// get death drops as list
			List<ItemStack> drops = event.getDrops();

			// get iterator of death drops list
			ListIterator<ItemStack> iterator = drops.listIterator();

			// create death compass stack for comparison
			ItemStack deathCompass = DeathCompass.createItem();

			// loop through all dropped items and remove any stacks that are death compasses
			while (iterator.hasNext()) {
				ItemStack stack = iterator.next();
				if (stack.isSimilar(deathCompass)) {
					iterator.remove();
				}
			}
		}

		Player player = event.getEntity();
		UUID playeruuid = player.getUniqueId();

		// if player world is not enabled in config, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create new death record for player
		DeathCompass deathRecord = new DeathCompass(player);
		
		// put death record in database
		plugin.dataStore.putRecord(deathRecord);
		
		// put player uuid in deathTriggeredRespawn hashset
		deathTriggeredRespawn.add(playeruuid);
	}

	
	/**
	 * Player respawn event handler
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		Player player = event.getPlayer();

		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if deathTriggeredRespawn hashset does not contain user uuid, do nothing and return
		if (!deathTriggeredRespawn.contains(player.getUniqueId())) {
			return;
		}
		
		// remove player uuid from deathTriggeredRespawn hashset
		deathTriggeredRespawn.remove(player.getUniqueId());
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// give player death compass
		giveDeathCompass(player);
		
		// set compass target to player death location
		setDeathCompassTarget(player);
		
		// send player respawn message
		plugin.messageManager.sendMessage(player, MessageId.ACTION_PLAYER_RESPAWN);
	}

	
	/**
	 * Player join event handler
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		
		// if player world is not enabled, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create 1 compass itemstack with configured settings
		ItemStack deathcompass = DeathCompass.createItem();
		
		// get player last death location
		Location lastdeathloc = getDeathLocation(player);
		
		// if player does not have at least one death compass in inventory or
		// saved death location in current world, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1) ||
				lastdeathloc == null) {
			return;
		}
		
		// set player compass target to last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Player change world event handler
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent event) {

		Player player = event.getPlayer();
		
		// if player world is not enabled in config, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create DeathCompass itemstack
		ItemStack deathcompass = DeathCompass.createItem();
		
		// get last death location from datastore
		Location lastDeathLocation = getDeathLocation(player);
		
		// if player does not have a death compass or saved death location, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1) ||
				lastDeathLocation == null) {
			return;
		}
		
		// set death compass target to player last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Player Interact event handler
	 * Remove all death compasses from player inventory on interaction with DeathChestBlocks
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		
		final Player player = event.getPlayer();
		final Block block = event.getClickedBlock();
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		// if block is not a DeathChestBlock, do nothing and return
		if (!(block != null && block.hasMetadata("deathchest-owner"))) {
			return;
		}
		
		// if deathchest owner is not player, do nothing and return
		if (!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
			return;
		}
		
		// remove all death compasses from player inventory
		removeDeathCompasses(player.getInventory());
		
		// reset compass target to world spawn
		resetDeathCompassTarget(player);
	}


	/**
	 * Item drop event handler
	 * @param event the event handled by this method
	 */
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		
		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		Player player = event.getPlayer();
		
		// get itemstack that was dropped
		ItemStack droppedItemStack = event.getItemDrop().getItemStack();
		
		// create death compass itemstack for comparison
		ItemStack dc = DeathCompass.createItem();

		// if droppedItemStack is not a DeathCompass or destroy-on-drop config is not true, do nothing and return
		if (!droppedItemStack.isSimilar(dc) || !plugin.getConfig().getBoolean("destroy-on-drop")) {
			return;
		}
		
		// remove dropped item
		event.getItemDrop().remove();
		
		// play item_break sound to player if sound effects enabled in config
		plugin.soundConfig.playSound(player, SoundId.PLAYER_DROP_COMPASS);

		// if inventory does not contain at least 1 death compass, reset compass target
		if (!player.getInventory().containsAtLeast(dc, 1)) {
			resetDeathCompassTarget(player);
		}
		
		// send player compass destroyed message
		plugin.messageManager.sendMessage(player, MessageId.ACTION_ITEM_DESTROY);
	}


	/**
	 * Remove player from cache on player quit event
	 * @param event event handled by this method
	 */
	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		plugin.dataStore.flushCache(event.getPlayer().getUniqueId());
	}


	/**
	 * Give 1 death compass to player
	 * @param player the player being given a death compass
	 */
	private void giveDeathCompass(Player player) {
		
		// create DeathCompass itemstack
		ItemStack deathcompass = DeathCompass.createItem();
		
		// add DeathCompass itemstack to player inventory
		player.getInventory().addItem(deathcompass);
		
		// log info
		plugin.getLogger().info(player.getName() + " was given a death compass in " + player.getWorld().getName() + ".");
		
	}

	
	/**
	 * Remove all death compasses from inventory
	 * @param inventory the inventory from which to remove all death compasses
	 */
	private void removeDeathCompasses(Inventory inventory) {
		ItemStack deathcompass = DeathCompass.createItem();
		inventory.removeItem(deathcompass);
	}

	
	/**
	 * Set death compass target
	 * delay for configured number of ticks (default 20)  to allow player to respawn
	 * @param player the player whose death location is being set as the compass target
	 */
	private void setDeathCompassTarget(final Player player) {
		new BukkitRunnable(){

			public void run() {
				Location location = getDeathLocation(player);
				if (location.getWorld() != player.getWorld()) {
					return;
				}
				player.setCompassTarget(location);
			}
		}.runTaskLaterAsynchronously(plugin, plugin.getConfig().getLong("target-delay"));
	}

	
	/**
	 * Reset compass target
	 * @param player the player whose compass target is being reset
	 */
	private void resetDeathCompassTarget(Player player) {
		
		// set player compass target to world spawn location
		player.setCompassTarget(player.getWorld().getSpawnLocation());
	}

	
	/**
	 * Retrieve player death location from datastore
	 * @param player the player whose death location is being retrieve
	 * @return location
	 */
	private Location getDeathLocation(Player player) {
		
		// set worldname to player current world
		String worldName = player.getWorld().getName();
	
		// set location to world spawn location
		Location location = player.getWorld().getSpawnLocation();

		// fetch death record from datastore
		DeathCompass deathRecord = plugin.dataStore.getRecord(player.getUniqueId(),worldName);

		if (deathRecord != null) {
			location = deathRecord.getLocation();
		}
		
		// return location
		return location;
	}

}
