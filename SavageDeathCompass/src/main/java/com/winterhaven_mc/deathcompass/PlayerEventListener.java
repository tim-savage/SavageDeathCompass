package com.winterhaven_mc.deathcompass;

import com.winterhaven_mc.deathcompass.DeathCompassMain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.Metadatable;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerEventListener implements Listener {
	
	private final DeathCompassMain plugin;
	private HashSet<String> deathTriggeredRespawn = new HashSet<String>();

	
	/**
	 * Class constructor
	 * @param plugin
	 */
	public PlayerEventListener(DeathCompassMain plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/**
	 * Player death event handler
	 * @param event
	 * @throws Exception 
	 */
	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerDeath(PlayerDeathEvent event) throws Exception {
		
		Player player = event.getEntity();
		String playeruuid = player.getUniqueId().toString();
		
		// if player world is not enabled in config, do nothing and return
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// put death location in database
		plugin.datastore.putRecord(player);
		
		// put player uuid in deathTriggeredRespawn hashset
		deathTriggeredRespawn.add(playeruuid);
		
		// if destroy-on-drop is enabled in configuration, remove any death compasses from player drops on death
		if (plugin.getConfig().getBoolean("destroy-on-drop",true)) {

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
	 * @param event
	 */
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent event) {
		
		Player player = event.getPlayer();

		//if player world is not enabled, do nothing and return
		if (!playerWorldEnabled(player)) {
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
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {

		Player player = event.getPlayer();
		
		// if player world is not enabled, do nothing and return
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create 1 compass itemstack with configured settings
		ItemStack deathcompass = createDeathCompassStack(1);
		
		// get player last death location from hashmap
		//plugin.deathlocations.loadDeathLocation(player);
		
		Location lastdeathloc = null;

		lastdeathloc = plugin.datastore.getRecord(player);
		
		// if player does not have at least one death compass in inventory or
		// entry in deathlocations hashmap, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1) || 
				lastdeathloc == null) {
			return;
		}
		
		// set player compass target to last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Player change world event handler
	 * @param event
	 */
	@EventHandler
	public void onChangeWorld(PlayerChangedWorldEvent event) {

		Player player = event.getPlayer();
		
		// if player world is not enabled in config, do nothing and return
		if (!playerWorldEnabled(player)) {
			return;
		}
		
		// if player does not have deathcompass.use permission, do nothing and return
		if (!player.hasPermission("deathcompass.use")) {
			return;
		}
		
		// create 1 death compass itemstack
		ItemStack deathcompass = createDeathCompassStack(1);
		
		// load player last death location
		//plugin.deathlocations.loadDeathLocation(player);

		// get last death location from datastore
		Location lastdeathloc = null;
		
		lastdeathloc = plugin.datastore.getRecord(player);
		
		// if player does not have a death compass or saved death location, do nothing and return
		if (!player.getInventory().containsAtLeast(deathcompass, 1) ||
				lastdeathloc == null) {
			return;
		}
		
		// set death compass target to player last death location
		setDeathCompassTarget(player);
	}


	/**
	 * Inventory open event handler
	 * Removes death compasses from inventories when a death chest is opened
	 * @param event
	 */
	@EventHandler
	public void onInventoryOpenEvent(InventoryOpenEvent event) {

		// if event is cancelled, do nothing and return
		if (event.isCancelled()) {
			return;
		}

		InventoryHolder inventoryItem = event.getInventory().getHolder();

		// if inventory item is a chest and has deathchest metadata,
		// remove all death compasses from chest and player inventory
		if (inventoryItem instanceof Chest && ((Metadatable) inventoryItem).hasMetadata("deathchest-owner")) {
			Player player = (Player)event.getPlayer();
			removeDeathCompasses(event.getInventory());
			removeDeathCompasses(player.getInventory());
			
			// reset compass target to home or spawn
			resetDeathCompassTarget(player);
		}
	}
	
	
	/**
	 * Block damage event handler<br>
	 * Remove all death compasses from player inventory on death chest damage
	 * @param event
	 */
	@EventHandler
	public void onBlockDamage(BlockDamageEvent event) {
	
		Player player = event.getPlayer();
		Block block = event.getBlock();
		
		// if block is not a DeathChestBlock, do nothing and return
		if (!(block instanceof Metadatable && block.hasMetadata("deathchest-owner"))) {
			return;
		}
		if (plugin.debug) {
			plugin.getLogger().info(player.getName() + " punched a deathchest block."); 
		}
		
		// if deathchest owner is not player, do nothing and return
		if (!block.getMetadata("deathchest-owner").get(0).asString().equals(player.getUniqueId().toString())) {
			if (plugin.debug) {
				plugin.getLogger().info("Deathchest block was not owned by player.");
			}
			return;
		}
		
		// remove all death compasses from player inventory
		removeDeathCompasses(player.getInventory());
		
		// reset compass target to home or spawn
		resetDeathCompassTarget(player);
	}


	/**
	 * Item drop event handler
	 * @param event
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
		if (!droppeditemstack.equals(dc) || !plugin.getConfig().getBoolean("destroy-on-drop", true)) {
			return;
		}
		
		// remove dropped item
		event.getItemDrop().remove();
		
		// play item_break sound to player
		player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
		
		// if inventory does not contain at least 1 death compass, reset compass target
		if (!player.getInventory().containsAtLeast(dc, 1)) {
			resetDeathCompassTarget(player);
		}
		
		// send player compass destroyed message
		plugin.messageManager.sendPlayerMessage(player, "destroy");
	}

	
	/**
	 * Give 1 death compass to player
	 * @param player
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
	 * @param inventory
	 */
	private void removeDeathCompasses(Inventory inventory) {
		ItemStack deathcompass = createDeathCompassStack(64);
		inventory.removeItem(deathcompass);
	}

	
	/**
	 * Create death compass itemstack with given quantity
	 * @param quantity
	 * @return
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
		itemname = ChatColor.translateAlternateColorCodes((char)'&', itemname);
		
		// get item lore from messages file
		List<String> itemlore = plugin.messageManager.getItemLore();
		
		// allow '&' as color code character
		ArrayList<String> coloredlore = new ArrayList<String>();
		for (String string : itemlore) {
			string = ChatColor.translateAlternateColorCodes((char)'&', string);
			coloredlore.add(string);
		}
		
		// create itemstack with given quantity and durability 1 (to differentiate from other compasses)
		// NOTE: removing the custom durability, as it is not compatible with 1.8
		// death compasses will now be identified solely by their item metadata (name and lore)
		ItemStack dc = new ItemStack(Material.COMPASS, quantity);

		// set item name and lore metadata
		ItemMeta dcMeta = dc.getItemMeta();
		dcMeta.setDisplayName((Object)ChatColor.RESET + itemname);
		dcMeta.setLore(coloredlore);
		dc.setItemMeta(dcMeta);
		return dc;
	}

	
	/**
	 * Set death compass target
	 * delay for 20 ticks to allow player to respawn
	 * @param player
	 */
	private void setDeathCompassTarget(final Player player) {
		new BukkitRunnable(){

			public void run() {
				Location myloc = null;
				myloc = plugin.datastore.getRecord(player);
				if (myloc.getWorld() != player.getWorld()) {
					return;
				}
				player.setCompassTarget(myloc);
			}
		}.runTaskLater(plugin, 20);
	}

	
	/**
	 * Reset compass target
	 * @param player
	 */
	private void resetDeathCompassTarget(Player player) {
		
		// test bed spawn location, otherwise use world spawn location
		Location newloc = player.getBedSpawnLocation();
		if (newloc == null) {
			newloc = player.getWorld().getSpawnLocation();
		}
		
		// set player compass target to location
		player.setCompassTarget(newloc);
	}

	
	/**
	 * Test if player world is enabled in config
	 * @param player
	 * @return boolean
	 */
	private boolean playerWorldEnabled(Player player) {
		
		// get list of enabled worlds from config
		List<String> enabledworlds = plugin.getConfig().getStringList("enabled-worlds");
		
		// if player world is in list of enabled worlds, return true
		if (enabledworlds.contains(player.getWorld().getName())) {
			return true;
		}
		
		// otherwise return false
		return false;
	}


}
