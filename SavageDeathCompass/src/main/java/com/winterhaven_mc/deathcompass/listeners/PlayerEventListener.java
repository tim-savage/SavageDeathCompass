package com.winterhaven_mc.deathcompass.listeners;

import com.winterhaven_mc.deathcompass.PluginMain;
import com.winterhaven_mc.deathcompass.storage.DeathRecord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PlayerEventListener implements Listener {

	// reference to main class
	private final PluginMain plugin;

	// player death respawn map
	private Set<String> deathTriggeredRespawn = new HashSet<String>();

	
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
		
		Player player = event.getEntity();
		String playeruuid = player.getUniqueId().toString();
		
		// if player world is not enabled in config, do nothing and return
		if (!plugin.worldManager.isEnabled(player.getWorld())) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create new death record for player
		DeathRecord deathRecord = new DeathRecord(player);
		
		// put death record in database
		plugin.dataStore.putRecord(deathRecord);
		
		// put player uuid in deathTriggeredRespawn hashset
		deathTriggeredRespawn.add(playeruuid);
		
		// if destroy-on-drop is enabled in configuration, remove any death compasses from player drops on death
		if (plugin.getConfig().getBoolean("destroy-on-drop")) {

			// get death drops as list
			List<ItemStack> drops = event.getDrops();

			// get iterator of death drops list
			ListIterator<ItemStack> iterator = drops.listIterator();

			// create death compass stack for comparison
			ItemStack deathCompass = createDeathCompassStack(1);

			// loop through all dropped items and remove any stacks that are death compasses
			while (iterator.hasNext()) {
				ItemStack stack = iterator.next();
				if (stack.isSimilar(deathCompass)) {
					iterator.remove();
				}
			}
		}
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
			if (plugin.debug) {
				plugin.getLogger().info("Player world " + player.getWorld().getName() + " not enabled.");
			}
			return;
		}
		
		// if deathTriggeredRespawn hashset does not contain user uuid, do nothing and return
		if (!deathTriggeredRespawn.contains(player.getUniqueId().toString())) {
			if (plugin.debug) {
				plugin.getLogger().info("player uuid not in deathTriggeredRespawn hashset.");
			}
			return;
		}
		
		// remove player uuid from deathTriggeredRespawn hashset
		deathTriggeredRespawn.remove(player.getUniqueId().toString());
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			if (plugin.debug) {
				plugin.getLogger().info("Player " + player.getName() + " does not have permission 'deathcompass.use'.");
			}
			return;
		}
		
		// give player death compass
		giveDeathCompass(player);
		
		// set compass target to player death location
		setDeathCompassTarget(player);
		
		// send player respawn message
		plugin.messageManager.sendPlayerMessage(player, "respawn");
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
		ItemStack deathcompass = createDeathCompassStack(1);
		
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
		
		// create 1 death compass itemstack
		ItemStack deathcompass = createDeathCompassStack(1);
		
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
		ItemStack droppeditemstack = event.getItemDrop().getItemStack();
		
		// create death compass itemstack for comparison
		ItemStack dc = createDeathCompassStack(droppeditemstack.getAmount());
		
		// if dropped itemstack is not deathcompass or destroy-on-drop config is not true, do nothing and return 
		if (!droppeditemstack.equals(dc) || !plugin.getConfig().getBoolean("destroy-on-drop")) {
			return;
		}
		
		// remove dropped item
		event.getItemDrop().remove();
		
		// play item_break sound to player if sound effects enabled in config
		if (plugin.getConfig().getBoolean("sound-effects")) {
			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
		}
		
		// if inventory does not contain at least 1 death compass, reset compass target
		if (!player.getInventory().containsAtLeast(dc, 1)) {
			resetDeathCompassTarget(player);
		}
		
		// send player compass destroyed message
		plugin.messageManager.sendPlayerMessage(player, "destroy");
	}

	
	/**
	 * Give 1 death compass to player
	 * @param player the player being given a death compass
	 */
	private void giveDeathCompass(Player player) {
		
		// create 1 death compass itemstack
		ItemStack deathcompass = createDeathCompassStack(1);
		
		// add death compass itemstack to player inventory
		player.getInventory().addItem(deathcompass);
		
		// log info
		plugin.getLogger().info(player.getName() + " was given a death compass in " + player.getWorld().getName() + ".");
		
	}

	
	/**
	 * Remove all death compasses from inventory
	 * @param inventory the inventory from which to remove all death compasses
	 */
	private void removeDeathCompasses(Inventory inventory) {
		ItemStack deathcompass = createDeathCompassStack(64);
		inventory.removeItem(deathcompass);
	}

	
	/**
	 * Create death compass itemstack with given quantity
	 * @param quantity the number of items in the ItemStack being created
	 * @return ItemStack of death compass item(s)
	 */
	private ItemStack createDeathCompassStack(int quantity) {
		if (quantity > Material.COMPASS.getMaxStackSize()) {
			quantity = Material.COMPASS.getMaxStackSize();
		}
		else if (quantity < 1) {
			quantity = 1;
		}
		// get item name from messages file
		String itemname = plugin.messageManager.getItemName();
		
		// allow '&' as color code character
		itemname = ChatColor.translateAlternateColorCodes('&', itemname);
		
		// get item lore from messages file
		List<String> itemlore = plugin.messageManager.getItemLore();
		
		// allow '&' as color code character
		ArrayList<String> coloredlore = new ArrayList<String>();
		for (String string : itemlore) {
			string = ChatColor.translateAlternateColorCodes('&', string);
			coloredlore.add(string);
		}
		
		// create itemstack with given quantity and durability 1 (to differentiate from other compasses)
		// NOTE: removing the custom durability, as it is not compatible with 1.8
		// death compasses will now be identified solely by their item metadata (name and lore)
		ItemStack dc = new ItemStack(Material.COMPASS, quantity);

		// set item name and lore metadata
		ItemMeta dcMeta = dc.getItemMeta();
		dcMeta.setDisplayName(ChatColor.RESET + itemname);
		dcMeta.setLore(coloredlore);
		dc.setItemMeta(dcMeta);
		return dc;
	}

	
	/**
	 * Set death compass target
	 * delay for 20 ticks to allow player to respawn
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
		DeathRecord deathRecord = plugin.dataStore.getRecord(player.getUniqueId(),worldName);

		if (deathRecord != null) {
			location = deathRecord.getLocation();
		}
		
		// return location
		return location;
	}

}
