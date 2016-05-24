package com.biggestnerd.devotedpvp;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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

public class MapManager implements Listener {
	
	private static MapManager instance;
	
	private HashMap<UUID, Location> posOnes;
	private HashMap<UUID, Location> posTwos;
	private HashMap<UUID, Location> spawnOnes;
	private HashMap<UUID, Location> spawnTwos;
	
	private HashSet<MapStructure> structures;
	private HashMap<Location, MapStructure> mapLocations;
	private HashMap<Location, Boolean> inUse;
	private HashMap<UUID, Location> users;
	private Location lastLocation;
	private Random rng;
	private File structureDir;
	
	private MapManager() {
		structures = new HashSet<MapStructure>();
		inUse = new HashMap<Location, Boolean>();
		mapLocations = new HashMap<Location, MapStructure>();
		rng = new Random();
		posOnes = new HashMap<UUID, Location>();
		posTwos = new HashMap<UUID, Location>();
		spawnOnes = new HashMap<UUID, Location>();
		spawnTwos = new HashMap<UUID, Location>();
		users = new HashMap<UUID, Location>();
		structureDir = new File(DevotedPvP.getInstance().getDataFolder(), "structures");
		if(!structureDir.exists()) {
			structureDir.mkdirs();
		} else {
			for(File f : structureDir.listFiles()) {
				MapStructure structure = MapStructure.load(f);
				if(structure != null) {
					structures.add(structure);
				}
			}
		}
		lastLocation = new Location(DevotedPvP.getInstance().getEndWorld(), 500, 50, 500);
	}
	
	public void randomSpawnForDuel(Player p1, Player p2) {
		Location loc = getNextUnusedLocation();
		MapStructure structure = mapLocations.get(loc);
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
		MapStructure[] entries = (MapStructure[]) structures.toArray();
		MapStructure structure = entries[rng.nextInt(entries.length)];
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
		structure.loadAtLocation(next);
		return next;
	}
	
	private void removeStructures() {
		for(Location loc : mapLocations.keySet()) {
			MapStructure map = mapLocations.get(loc);
			map.remove(loc);
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
					if(event.getPlayer().isSneaking()) {
						spawnOnes.put(id, event.getClickedBlock().getLocation());
					} else {
						posOnes.put(id, event.getClickedBlock().getLocation());
					}
				}
				if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
					if(event.getPlayer().isSneaking()) {
						spawnTwos.put(id, event.getClickedBlock().getLocation());
					} else {
						posTwos.put(id, event.getClickedBlock().getLocation());
					}
				}
			}
		}
	}
	
	public void makeStructure(Player player, String name) {
		UUID id = player.getUniqueId();
		if(!(spawnOnes.containsKey(id) && posOnes.containsKey(id) && spawnTwos.containsKey(id) && posTwos.containsKey(id))) {
			player.sendMessage(ChatColor.RED + "You must specify four positions to create a structure");
		} else {
			MapStructure structure = new MapStructure(name, posOnes.get(id), posTwos.get(id), spawnOnes.get(id), spawnTwos.get(id));
			structures.add(structure);
			File file = new File(structureDir, name + ".mbs");
			if(!file.isFile()) {
				try {
					file.createNewFile();
					structure.save(file);
				} catch (IOException ex) {
					ex.printStackTrace();
				}
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
