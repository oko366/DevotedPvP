package com.biggestnerd.devotedpvp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

public class MapLoader {

	private static File mapsFolder;
	private static File rootFolder;
	
	private String name;
	private Location spawn1;
	private Location spawn2;
	
	public MapLoader(String name, Location spawn1, Location spawn2) {
		if(mapsFolder == null) {
			mapsFolder = new File(DevotedPvP.getInstance().getDataFolder() + "/maps");
			if(!mapsFolder.isDirectory()) {
				mapsFolder.mkdirs();
			}
			rootFolder = mapsFolder.getParentFile().getParentFile().getParentFile();
		}
		this.name = name;
		this.spawn1 = spawn1;
		this.spawn2 = spawn2;
	}
	
	public Location getFirstSpawn() {
		return spawn1;
	}
	
	public Location getSecondSpawn() {
		return spawn2;
	}
	
	public String getName() {
		return name;
	}
	
	public String loadAndTeleportPlayers(Player p1, Player p2) {
		File copyFile = new File(mapsFolder, name);
		if(!copyFile.exists()) return "";
		String worldName = "duel_" + p1.getName() + "_" + p2.getName();
		Bukkit.getServer().unloadWorld(worldName, true);
		File worldFile = new File(rootFolder, worldName);
		if(worldFile.exists()) {
			try {
				FileUtils.deleteDirectory(worldFile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		try {
			FileUtils.copyDirectory(copyFile, worldFile);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		Bukkit.getServer().createWorld(new WorldCreator(worldName));
		World duelWorld = Bukkit.getWorld(worldName);
		if(duelWorld != null) {
			duelWorld.setDifficulty(Difficulty.HARD);
			duelWorld.setGameRuleValue("doMobSpawning", "false");
			spawn1.setWorld(duelWorld);
			spawn2.setWorld(duelWorld);
			p1.teleport(spawn1);
			p2.teleport(spawn2);
			return worldName;
		} else {
			worldFile.setWritable(true);
			try {
				FileUtils.deleteDirectory(worldFile);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return loadAndTeleportPlayers(p1, p2);
		}
	}

	public static MapLoader copyWorldFolder(Player player, String name) {
		File source = player.getWorld().getWorldFolder();
		File target = new File(mapsFolder, name);
		source.setWritable(true);
		Location loc = player.getLocation();
		player.teleport(DevotedPvP.getInstance().getSpawnWorld().getSpawnLocation());
		Bukkit.unloadWorld(player.getName(), true);
		try {
			FileUtils.copyDirectory(source, target);
			FileUtils.deleteDirectory(source);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new MapLoader(name, loc, loc);
	}
	
	public static void remove(String name) {
		File remove = new File(mapsFolder, name);
		try {
			FileUtils.deleteDirectory(remove);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void cleanUp() {
		if (rootFolder == null) {
			System.err.println("Failed to cleanup; root directory missing or misconfigured?");
			return;
		}
		if (rootFolder.listFiles() == null) {
			return;
		}
		for(File file : rootFolder.listFiles()) {
			if(file.getName().startsWith("dir_")) {
				try {
					FileUtils.deleteDirectory(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static File getRootFolder() {
		return rootFolder;
	}
}
