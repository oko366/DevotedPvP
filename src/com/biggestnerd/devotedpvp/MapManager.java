package com.biggestnerd.devotedpvp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import net.minecraft.server.v1_11_R1.MinecraftServer;
import net.minecraft.server.v1_11_R1.WorldServer;

public class MapManager implements Listener {
	
	private static MapManager instance;

	private HashMap<String, MapLoader> loaders;
	private HashMap<UUID, String> users;
	private Random rng;
	private File configFile;
	
	private MapManager() {
		loaders = new HashMap<String, MapLoader>();
		users = new HashMap<UUID, String>();
		rng = new Random();
		loadMaps();
	}
	
	private void loadMaps() {
		configFile = new File(DevotedPvP.getInstance().getDataFolder(), "maps.yml");
		if(!configFile.exists()) {
			try {
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		for(String map : config.getKeys(false)) {
			ConfigurationSection mapConfig = config.getConfigurationSection(map);
			int x1 = mapConfig.getInt("x1");
			int y1 = mapConfig.getInt("y1");
			int z1 = mapConfig.getInt("z1");
			int x2 = mapConfig.getInt("x2");
			int y2 = mapConfig.getInt("y2");
			int z2 = mapConfig.getInt("z2");
			Location spawn1 = new Location(DevotedPvP.getInstance().getSpawnWorld(), x1, y1, z1);
			Location spawn2 = new Location(DevotedPvP.getInstance().getSpawnWorld(), x2, y2, z2);
			loaders.put(map, new MapLoader(map, spawn1, spawn2));
		}
	}
	
	public void randomSpawnForDuel(Player p1, Player p2) {
		System.out.println("Preparing duel map for " + p1.getName() + " and " + p2.getName());
		MapLoader loader = getRandomMap();
		String map = loader.loadAndTeleportPlayers(p1, p2);
		users.put(p1.getUniqueId(), map);
	}
	
	public void handleDuelEnd(UUID winner, UUID loser) {
		String worldName = users.get(winner);
		if(worldName == null) {
			worldName = users.get(loser);
		}
		World world = Bukkit.getWorld(worldName);
		File dir = world.getWorldFolder();
		Bukkit.unloadWorld(worldName, true);
		MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
		WorldServer worldServer = ((CraftWorld)world).getHandle();
		server.worlds.remove(worldServer);
		try {
			dir.setWritable(true);
			FileUtils.deleteDirectory(dir);
			System.out.println("Deleted: " + worldName);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private MapLoader getRandomMap() {
		String[] names = loaders.keySet().toArray(new String[0]);
		return loaders.get(names[rng.nextInt(names.length)]);
	}
	
	public void listMaps(Player player) {
		String list = "";
		for(String map : loaders.keySet()) {
			list += map + ", ";
		}
		player.sendMessage(ChatColor.GREEN + "The following maps are configured: " + list.substring(0, list.length() - 2));
	}
	
	public void createBlankMap(Player player) {
		File emptyWorld = new File(DevotedPvP.getInstance().getDataFolder(), "empty");
		if(!emptyWorld.exists()) {
			player.sendMessage(ChatColor.RED + "EmptyWorld folder is missing!");
			return;
		}
		File worldFile = new File(MapLoader.getRootFolder(), player.getName());
		try {
			FileUtils.copyDirectory(emptyWorld, worldFile);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		Bukkit.getServer().createWorld(new WorldCreator(player.getName()));
		World world = Bukkit.getWorld(player.getName());
		player.teleport(new Location(world, 0, 40, 0));
	}
	
	public void createMapLoader(Player player, String name) {
		MapLoader loader = MapLoader.copyWorldFolder(player, name);
		if(loader == null) {
			player.sendMessage(ChatColor.RED + "An error occurred making the map!");
			return;
		}
		loaders.put(name, loader);
		saveLoader(loader);
		player.sendMessage(ChatColor.GREEN + "Map '" + name + "' created!");
	}
	
	public void deleteMap(Player player, String name) {
		loaders.remove(name);
		MapLoader.remove(name);
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.set(name, null);
		player.sendMessage(ChatColor.GREEN + "Map deleted: " + name);
	}
	
	public void reload(Player player) {
		loaders.clear();
		loadMaps();
		player.sendMessage(ChatColor.GREEN + "Maps reloaded!");
	}
	
	private void saveLoader(MapLoader loader) {
		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		ConfigurationSection mapSection = config.createSection(loader.getName());
		Location spawn1 = loader.getFirstSpawn();
		Location spawn2 = loader.getSecondSpawn();
		mapSection.set("x1", spawn1.getBlockX());
		mapSection.set("y1", spawn1.getBlockY());
		mapSection.set("z1", spawn1.getBlockZ());
		mapSection.set("x2", spawn2.getBlockX());
		mapSection.set("y2", spawn2.getBlockY());
		mapSection.set("z2", spawn2.getBlockZ());
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static MapManager getInstance() {
		if(instance == null) {
			instance = new MapManager();
		}
		return instance;
	}
}
