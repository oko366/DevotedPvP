package com.biggestnerd.devotedpvp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
					+ "armor blob,"
					+ "contents blob,"
					+ "owner VARCHAR(36) NOT NULL)");
		getInventory = db.prepareStatement("SELECT * FROM inventories WHERE name=?");
		addInventory = db.prepareStatement("INSERT INTO inventories (name, armor, contents, owner) VALUES (?,?,?,?)");
		updateOwner = db.prepareStatement("UPDATE inventories SET owner=? WHERE name=?");
		deleteInventory = db.prepareStatement("DELETE FROM inventories WHERE name=?");
	}
	
	public boolean saveInventory(Player player, String kitName) {
		try {
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId())) {
					PreparedStatement ps = db.prepareStatement("UPDATE inventories SET armor=?, contents=? WHERE name=?");
					PlayerInventory inv = player.getInventory();
					ps.setBytes(1, SerializationUtils.serialize(inv.getArmorContents()));
					ps.setBytes(2, SerializationUtils.serialize(inv.getContents()));
					ps.setString(3, kitName);
					ps.execute();
					return true;
				}
			}
			addInventory.setString(1, kitName);
			addInventory.setString(4, player.getUniqueId().toString());
			PlayerInventory inv = player.getInventory();
			addInventory.setBytes(2, SerializationUtils.serialize(inv.getArmorContents()));
			addInventory.setBytes(3, SerializationUtils.serialize(inv.getContents()));
			addInventory.execute();
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public boolean loadInventory(Player player, String kitName) {
		try {
			getInventory.setString(1, kitName);
			ResultSet result = getInventory.executeQuery();
			if(result.next()) {
				player.getInventory().setArmorContents((ItemStack[])SerializationUtils.deserialize(result.getBytes("armor")));
				player.getInventory().setContents((ItemStack[])SerializationUtils.deserialize(result.getBytes("contents")));
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
