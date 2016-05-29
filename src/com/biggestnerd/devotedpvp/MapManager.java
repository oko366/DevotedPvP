package com.biggestnerd.devotedpvp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.biggestnerd.devotedpvp.MapStructure.InternalLocation;

public class MapManager implements Listener {
	
	private static MapManager instance;
	
	private HashMap<UUID, Location> posOnes;
	private HashMap<UUID, Location> posTwos;
	
	private HashMap<String, MapStructure> structures;
	private HashMap<Location, String> mapLocations;
	private HashMap<Location, Boolean> inUse;
	private HashMap<UUID, Location> users;
	private Location lastLocation;
	private Random rng;
	private Database db;
	
	private MapManager() {
		db = DevotedPvP.getInstance().getDb();
		MapStructure.setupWorldEditAdapter();
		structures = new HashMap<String, MapStructure>();
		inUse = new HashMap<Location, Boolean>();
		mapLocations = new HashMap<Location, String>();
		rng = new Random();
		posOnes = new HashMap<UUID, Location>();
		posTwos = new HashMap<UUID, Location>();
		users = new HashMap<UUID, Location>();
		initializeTables();
		loadCache();
		lastLocation = new Location(DevotedPvP.getInstance().getSpawnWorld(), 0, 70, 2500);
	}
	
	private void initializeTables() {
		db.execute("CREATE TABLE IF NOT EXISTS maps (name varchar(40) unique not null, x1 int, x2 int, y1 int, y2 int, z1 int, z2 int)");
	}
	
	private void loadCache() {
		try {
			PreparedStatement getMaps = db.prepareStatement("SELECT * FROM maps");
			ResultSet result = getMaps.executeQuery();
			while(result.next()) {
				String name = result.getString("name");
				InternalLocation spawn1 = new InternalLocation(result.getInt("x1"), result.getInt("y1"), result.getInt("z1"));
				InternalLocation spawn2 = new InternalLocation(result.getInt("x2"), result.getInt("y2"), result.getInt("z2"));
				structures.put(name, new MapStructure(name, spawn1, spawn2));
			}
		} catch (Exception ex) { 
			ex.printStackTrace();
		}
	}
	
	public void randomSpawnForDuel(Player p1, Player p2) {
		Location loc = getNextUnusedLocation();
		MapStructure structure = structures.get(mapLocations.get(loc));
		Location firstSpawn = structure.getFirstSpawn().getRelativeTo(loc.getBlock()).getLocation();
		Location secondSpawn = structure.getSecondSpawn().getRelativeTo(loc.getBlock()).getLocation();
		p1.teleport(firstSpawn, TeleportCause.PLUGIN);
		p2.teleport(secondSpawn, TeleportCause.PLUGIN);
		users.put(p1.getUniqueId(), loc);
	}
	
	public void handleDuelEnd(UUID winner, UUID loser) {
		Location loc = users.get(winner);
		if(loc == null) {
			loc = users.get(loser);
		}
		inUse.put(loc, false);
	}
	
	private static boolean x = false;
	private static boolean z = false;
	private static int mult = 1;
	private Location getNextUnusedLocation() {
		for(Location loc : inUse.keySet()) {
			if(!inUse.get(loc).booleanValue()) {
				return loc;
			}
		}
		String[] entries = structures.keySet().toArray(new String[0]);
		String structure = entries[rng.nextInt(entries.length)];
		int amt = 300 * mult++;
		Location next = lastLocation;
		if(!x) {
			x = true;
			next = next.add(amt, 0, 0);
		} else if(!z) {
			z = true;
			next = next.add(0, 0, amt);
		} else {
			x = false;
			z = false;
			next = next.add(amt, 0, amt);
		}
		mapLocations.put(next, structure);
		inUse.put(next, true);
		structures.get(structure).loadAtLocation(next);
		return next;
	}
	
	private void removeStructures() {
		for(Location loc : mapLocations.keySet()) {
			MapStructure map = structures.get(mapLocations.get(loc));
			map.unload(loc);
		}
	}
	
	@EventHandler
	public void onWorldUnload(WorldUnloadEvent event) {
		removeStructures();
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(event.getItem() != null) {
			ItemStack item = event.getItem();
			if(item.getType() == Material.STICK && item.getItemMeta().getLore().contains(ChatColor.GOLD + "this is a wand")) {
				UUID id = event.getPlayer().getUniqueId();
				if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					posOnes.put(id, event.getClickedBlock().getLocation());
					event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Spawn position one set!");
				}
				if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
					posTwos.put(id, event.getClickedBlock().getLocation());
					event.setCancelled(true);
					event.getPlayer().sendMessage(ChatColor.DARK_AQUA + "Spawn position two set!");
				}
			}
		}
	}
	
	public void makeStructure(Player player, String name) {
		UUID id = player.getUniqueId();
		if(!(posOnes.containsKey(id) && posTwos.containsKey(id))) {
			player.sendMessage(ChatColor.RED + "You must specify spawn positions to create a structure");
		} else {
			try {
				Location pos = player.getLocation();
				Location posOne = posOnes.get(id);
				Location posTwo = posTwos.get(id);
				int x1 = Math.abs(posOne.getBlockX() -  pos.getBlockX());
				int y1 = Math.abs(posOne.getBlockY() - pos.getBlockY());
				int z1 = Math.abs(posOne.getBlockZ() - pos.getBlockZ());
				int x2 = Math.abs(posTwo.getBlockX() -  pos.getBlockX());
				int y2 = Math.abs(posTwo.getBlockY() - pos.getBlockY());
				int z2 = Math.abs(posTwo.getBlockZ() - pos.getBlockZ());
				MapStructure structure = new MapStructure(name, new InternalLocation(x1, y1, z1), new InternalLocation(x2, y2, z2));
				PreparedStatement insert = db.prepareStatement("INSERT INTO maps (name,x1,y1,z1,x2,y2,z2) VALUES (?,?,?,?,?,?,?)");
				insert.setString(1, name);
				insert.setInt(2, x1);
				insert.setInt(3, y1);
				insert.setInt(4, z1);
				insert.setInt(5, x2);
				insert.setInt(6, y2);
				insert.setInt(7, z2);
				insert.execute();
				structures.put(name, structure);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void giveWand(Player player) {
		ItemStack wand = new ItemStack(Material.STICK);
		ItemMeta meta = wand.getItemMeta();
		meta.setLore(new LinkedList<String>(Arrays.asList(new String[]{ChatColor.GOLD + "this is a wand"})));
		meta.setDisplayName("Wand");
		wand.setItemMeta(meta);
		int emptySlot = player.getInventory().firstEmpty();
		if(emptySlot != -1) {
			player.getInventory().setItem(emptySlot, wand);
		} else {
			player.sendMessage(ChatColor.RED + "Inventory full, could not give wand");
		}
	}
	
	public static MapManager getInstance() {
		if(instance == null) {
			instance = new MapManager();
		}
		return instance;
	}
}
