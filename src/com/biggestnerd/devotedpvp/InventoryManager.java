package com.biggestnerd.devotedpvp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R1.NBTTagCompound;

public class InventoryManager {

	private static InventoryManager instance;
	
	private Database db;
	private DevotedPvP plugin;
	
	private PreparedStatement getInventory;
	private PreparedStatement addInventory;
	private PreparedStatement updateOwner;
	private PreparedStatement deleteInventory;
	
	private InventoryManager() {
		plugin = DevotedPvP.getInstance();
		db = plugin.getDb();
		setupTables();
	}
	
	private void setupTables() {
		db.execute("CREATE TABLE IF NOT EXISTS inventories ("
					+ "name VARCHAR(40) UNIQUE NOT NULL,"
					+ "inv blob,"
					+ "owner VARCHAR(36) NOT NULL)");
		getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
		addInventory = db.prepareStatement("INSERT INTO inventories (name, inv, owner) VALUES (?,?,?)");
		updateOwner = db.prepareStatement("UPDATE inventories SET owner=? WHERE name=?");
		deleteInventory = db.prepareStatement("DELETE FROM inventories WHERE name=?");
	}
	
	public boolean saveInventory(Player player, String kitName) {
		if(!(player instanceof EntityHuman)) {
			System.out.println("For some reason, " + player.getName() + " is not a human, RIP");
			return false;
		}
		EntityHuman human = (EntityHuman) player;
		try {
			NBTTagCompound nbt = new NBTTagCompound();
			human.e(nbt);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			NBTCompressedStreamTools.a(nbt, out);
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId())) {
					PreparedStatement ps = db.prepareStatement("UPDATE inventories SET inv=? WHERE name=?");
					addInventory.setBytes(2, out.toByteArray());
					ps.setString(3, kitName);
					ps.execute();
					return true;
				}
			}
			
			addInventory.setString(1, kitName);
			addInventory.setString(3, player.getUniqueId().toString());
			addInventory.setBytes(2, out.toByteArray());
			addInventory.execute();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean loadInventory(Player player, String kitName) {
		if(!(player instanceof EntityHuman)) {
			System.out.println("For some reason, " + player.getName() + " is not a human, RIP");
			return false;
		}
		EntityHuman human = (EntityHuman) player;
		try {
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				NBTTagCompound nbt = null;
				ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes("inv"));
				nbt = NBTCompressedStreamTools.a(input);
				if(nbt != null) {
					human.f(nbt);
				}
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean transferInventory(Player owner, Player newOwner, String kitName) {
		try {
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(owner.getUniqueId())) {
					updateOwner.setString(1, newOwner.getUniqueId().toString());
					updateOwner.setString(2, kitName);
					updateOwner.execute();
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean inventoryExists(String kitName) {
		try {
			getInventory.setString(1, kitName);
			return getInventory.executeQuery().next();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean deleteInventory(Player player, String kitName) {
		try {
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId())) {
					deleteInventory.setString(1, kitName);
					deleteInventory.execute();
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public static InventoryManager getInstance() {
		if(instance == null) {
			instance = new InventoryManager();
		}
		return instance;
	}
}
