package com.biggestnerd.devotedpvp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

public class MapStructure {
	
	private static WorldEditPlugin wePlugin;
	private static WorldEditAdapter weAdapter;
	private static DummyPlayer mapSpawner;

	private String name;
	private InternalLocation spawn1;
	private InternalLocation spawn2;
	
	public MapStructure(String name, InternalLocation spawn1, InternalLocation spawn2) {
		this.name = name;
		this.spawn1 = spawn1;
		this.spawn2 = spawn2;
	}
	
	public InternalLocation getFirstSpawn() {
		return spawn1;
	}
	
	public InternalLocation getSecondSpawn() {
		return spawn2;
	}
	
	public String getName() {
		return name;
	}
	
	public void loadAtLocation(Location loc) {
		if(weAdapter == null) return;
		if(mapSpawner == null) {
			mapSpawner = new DummyPlayer("@MapSpawner", Bukkit.getServer(), loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
			weAdapter.createSession(mapSpawner.getUniqueId(), wePlugin.wrapPlayer(mapSpawner).getSessionKey());
		} else {
			mapSpawner.teleport(loc);
		}		
		Bukkit.getServer().dispatchCommand(mapSpawner, "//schematic load " + name);
		Bukkit.getServer().dispatchCommand(mapSpawner, "//paste");
		
	}
	
	public void unload(Location loc) {
		//does nothing atm lol
	}

	static class InternalLocation {
		int x, y, z;
		
		public InternalLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Block getRelativeTo(Block other) {
			return other.getRelative(x, y, z);
		}
	}
	
	public static void setupWorldEditAdapter() {
		wePlugin = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		if(wePlugin != null) {
			try {
				weAdapter = new WorldEditAdapter(wePlugin.getWorldEdit().getSessionManager());
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
	}
}
