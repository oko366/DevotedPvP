package com.biggestnerd.devotedpvp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.material.MaterialData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class MapStructure {

	private String name;
	private HashMap<InternalLocation, MaterialData> blocks;
	private InternalLocation firstSpawn;
	private InternalLocation secondSpawn;
	
	@SuppressWarnings("deprecation")
	public MapStructure(String name, Location pos1, Location pos2, Location spawnOne, Location spawnTwo) {
		this.name = name;
		blocks = new HashMap<InternalLocation, MaterialData>();
		int minx = Math.min(pos1.getBlockX(), pos2.getBlockX());
		int miny = Math.min(pos1.getBlockY(), pos2.getBlockY());
		int minz = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
		int xsize = Math.abs(pos1.getBlockX() - pos2.getBlockX());
		int ysize = Math.abs(pos1.getBlockY() - pos2.getBlockY());
		int zsize = Math.abs(pos1.getBlockZ() - pos2.getBlockZ());
		firstSpawn = new InternalLocation(Math.abs(spawnOne.getBlockX() - minx), Math.abs(spawnOne.getBlockY() - miny), Math.abs(spawnOne.getBlockZ() - minz));
		secondSpawn = new InternalLocation(Math.abs(spawnTwo.getBlockX() - minx), Math.abs(spawnTwo.getBlockY() - miny), Math.abs(spawnTwo.getBlockZ() - minz));
		for(int x = 0; x < xsize; x++) {
			for(int z = 0; z < zsize; z++) {
				for(int y = 0; y < ysize; y++) {
					Block block =  pos1.getWorld().getBlockAt(minx + x, miny + y, minz + z);
					blocks.put(new InternalLocation(x, y, z), new MaterialData(block.getType(), block.getData()));
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	public void loadAtLocation(Location loc) {
		Block source = loc.getBlock();
		for(InternalLocation il : blocks.keySet()) {
			Block block = il.getRelativeTo(source);
			MaterialData data = blocks.get(il);
			block.setType(data.getItemType());
			block.setData(data.getData());
		}
	}
	
	public void remove(Location loc) {
		Block source = loc.getBlock();
		for(InternalLocation il : blocks.keySet()) {
			Block block = il.getRelativeTo(source);
			block.setType(Material.AIR);
		}
	}
	
	public InternalLocation getFirstSpawn() {
		return firstSpawn;
	}
	
	public InternalLocation getSecondSpawn() {
		return secondSpawn;
	}
	
	public String getName() {
		return name;
	}
	
	public void save(File f) {
		try {
			Gson gson = new GsonBuilder().enableComplexMapKeySerialization().setPrettyPrinting().create();
			FileWriter fw = new FileWriter(f);
			fw.write(gson.toJson(this));
			fw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static MapStructure load(File f) {
		try {
			Gson gson = new Gson();
			return gson.fromJson(new FileReader(f), MapStructure.class);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	class InternalLocation {
		int x, y, z;
		
		public InternalLocation(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public Block getRelativeTo(Block other) {
			return other.getRelative(x, y, z);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + x;
			result = prime * result + y;
			result = prime * result + z;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			InternalLocation other = (InternalLocation) obj;
			if (x != other.x)
				return false;
			if (y != other.y)
				return false;
			if (z != other.z)
				return false;
			return true;
		}
	}
}
