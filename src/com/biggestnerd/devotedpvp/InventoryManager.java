package com.biggestnerd.devotedpvp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_9_R1.NBTTagCompound;

public class InventoryManager {

	private static InventoryManager instance;
	
	private Database db;
	private DevotedPvP plugin;
	
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
	}
	
	public boolean saveInventory(Player player, String kitName) {
		if(!(player instanceof CraftPlayer)) {
			System.out.println("For some reason, " + player.getName() + " is not a human, RIP");
			return false;
		}
		CraftPlayer craft = (CraftPlayer) player;
		EntityHuman human = craft.getHandle();
		try {
			NBTTagCompound nbt = new NBTTagCompound();
			human.e(nbt);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			NBTCompressedStreamTools.a(nbt, out);
			PreparedStatement getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId())) {
					PreparedStatement updateInventory = db.prepareStatement("UPDATE inventories SET inv=? WHERE name=?");
					updateInventory.setBytes(1, out.toByteArray());
					updateInventory.setString(2, kitName);
					updateInventory.execute();
					return true;
				}
			}
			PreparedStatement addInventory = db.prepareStatement("INSERT INTO inventories (name, inv, owner) VALUES (?,?,?)");
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
		if(!(player instanceof CraftPlayer)) {
			System.out.println("For some reason, " + player.getName() + " is not a human, RIP");
			return false;
		}
		CraftPlayer craft = (CraftPlayer) player;
		EntityPlayer human = craft.getHandle();
		try {
			PreparedStatement getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
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
			PreparedStatement getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(owner.getUniqueId())) {
					PreparedStatement updateOwner = db.prepareStatement("UPDATE inventories SET owner=? WHERE name=?");
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
			PreparedStatement getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
			getInventory.setString(1, kitName);
			return getInventory.executeQuery().next();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean deleteInventory(Player player, String kitName) {
		try {
			PreparedStatement getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId())) {
					PreparedStatement deleteInventory = db.prepareStatement("DELETE FROM inventories WHERE name=?");
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
