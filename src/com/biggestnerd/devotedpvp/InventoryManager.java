package com.biggestnerd.devotedpvp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.UUID;
import java.util.Map;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.Material;
import org.bukkit.Bukkit;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;

public class InventoryManager {

	private static InventoryManager instance;
	
	private Database db;
	private DevotedPvP plugin;
	private LinkedList<String> inventories;
	
	private InventoryManager() {
		plugin = DevotedPvP.getInstance();
		db = plugin.getDb();
		setupTables();
		inventories = new LinkedList<String>();
		try {
			PreparedStatement getAll = db.prepareStatement("SELECT * FROM inventories");
			ResultSet result = getAll.executeQuery();
			while(result.next()) {
				inventories.add(result.getString("name"));
			}
		} catch (Exception ex) {}
		fixVersion();
	}
	
	private void setupTables() {
		db.execute("CREATE TABLE IF NOT EXISTS inventories ("
					+ "name VARCHAR(40) UNIQUE NOT NULL,"
					+ "inv blob,"
					+ "owner VARCHAR(36) NOT NULL)");
		db.execute("CREATE TABLE IF NOT EXISTS db_version (db_version int not null," +
						"update_time varchar(24),"
						+ "plugin_name varchar(40) UNIQUE)");
		db.execute("INSERT IGNORE INTO db_version VALUES (1, now(), 'DevotedPvP')");
	}
	
	private void fixVersion() {
		int version = getVersion();
		if(version == 0) {
			try (PreparedStatement getInventories = db.prepareStatement("SELECT * FROM inventories");
					ResultSet result = getInventories.executeQuery();) {
				while(result.next()) {
					try {
						ByteArrayInputStream input = new ByteArrayInputStream(result.getBytes("inv"));
						NBTTagCompound nbt = NBTCompressedStreamTools.a(input);
						NBTBase inv = nbt.get("Inventory");
						NBTTagCompound parent = new NBTTagCompound();
						parent.set("inventory", inv);
						PreparedStatement updateInventory = db.prepareStatement("UPDATE inventories SET inv=? WHERE name=?");
						ByteArrayOutputStream output = new ByteArrayOutputStream();
						NBTCompressedStreamTools.a(parent, output);
						updateInventory.setString(1, result.getString("name"));
						updateInventory.setBytes(2, output.toByteArray());
						updateInventory.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			db.execute("UPDATE db_version SET db_version=1 WHERE plugin_name='DevotedPvP'");
		}
	}
	
	private int getVersion() {
		try {
			PreparedStatement getVersion = db.prepareStatement("SELECT * FROM db_version WHERE plugin_name='DevotedPvP'");
			ResultSet set = getVersion.executeQuery();
			if(set.next()) {
				return set.getInt("db_version");
			} else {
				return 0;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return 0;
	}
	
	public boolean saveInventory(Player player, String kitName) {
		if(!(player instanceof CraftPlayer)) {
			System.out.println("For some reason, " + player.getName() + " is not a human, RIP");
			return false;
		}
		if (!cleanInventory(player)) {
			System.out.println("For some reason, " + player.getName() + " has an uncleanable kit, RIP");
			return false;
		}
		if(kitName.length() > 40) {
			kitName = kitName.substring(0, 40);
		}
		CraftPlayer craft = (CraftPlayer) player;
		EntityHuman human = craft.getHandle();
		try {
			NBTTagCompound nbt = new NBTTagCompound();
			human.save(nbt);
			NBTTagCompound invNBT = new NBTTagCompound();
			invNBT.set("inventory", nbt.get("Inventory"));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			NBTCompressedStreamTools.a(invNBT, out);
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
			if(!inventories.contains(kitName)) {
				inventories.add(kitName);
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
				if(nbt == null) {
					return false;
				}	
				NBTTagCompound parent = new NBTTagCompound();
				human.save(parent);
				parent.set("Inventory", nbt.get("inventory"));
				human.f(parent);
				return cleanInventory(player);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	/**
	 * Sanitizes a player's inventory.
	 *
	 * Currently:
	 *  * Stack Size
	 *  * Enchantments
	 *  * Potion effect
	 *  * Eliminate exploit blocks (bedrock, dragon egg)
	 */
	public static boolean cleanInventory(Player player) {
		try {
			if (player == null) return true;
			PlayerInventory inventory = player.getInventory();
			if (inventory == null) return true;
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack item = inventory.getItem(i);
				// Allow external plugins to vouch for an item.
				ItemSafeEvent event = new ItemSafeEvent(item);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isValid()) continue;
				// done vouch.
				if (item == null) continue;
				if (Material.BEDROCK.equals(item.getType()) || Material.BARRIER.equals(item.getType()) ||
						Material.DRAGON_EGG.equals(item.getType()) || 
						Material.END_CRYSTAL.equals(item.getType()) ||
						Material.END_GATEWAY.equals(item.getType()) ||
						Material.ENDER_PORTAL.equals(item.getType()) ||
						Material.PORTAL.equals(item.getType()) ||
						Material.STRUCTURE_BLOCK.equals(item.getType()) ||
						Material.STRUCTURE_VOID.equals(item.getType())) {
					inventory.clear(i);
					continue;
				}
				// Create NBT-clear copy.
				ItemStack clone = new ItemStack(item.getType(), item.getAmount(), item.getDurability());

				// Sanitize Stack Size
				if (clone.getMaxStackSize() > 0 && item.getAmount() > clone.getMaxStackSize()) {
					clone.setAmount(clone.getMaxStackSize());
				} else if (clone.getMaxStackSize() < 0 && item.getAmount() > 1) {
					clone.setAmount(1);
				}

				// Sanitize Enchantments
				Map<Enchantment, Integer> ench = item.getEnchantments();
				if (ench != null && ench.size() > 0) {
					ench.forEach( (k, v) -> {
						try {
							clone.addEnchantment(k, v);
						} catch (IllegalArgumentException iae) {
						}
					});
				}

				// Sanitize potion effects
				if (item.hasItemMeta()) {
					ItemMeta meta = item.getItemMeta();
					if (meta instanceof PotionMeta) {
						PotionMeta pot = ((PotionMeta) meta);
						pot.clearCustomEffects();

						clone.setItemMeta(pot);
					}
				}
				inventory.setItem(i, clone);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
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
				if(UUID.fromString(result.getString("owner")).equals(player.getUniqueId()) || player.hasPermission("pvp.badmin")) {
					PreparedStatement deleteInventory = db.prepareStatement("DELETE FROM inventories WHERE name=?");
					deleteInventory.setString(1, kitName);
					deleteInventory.execute();
					inventories.remove(kitName);
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}
	
	public void listInventories(Player player, int page) {
		int pages = (inventories.size() / 10) + 1;
		if(page > pages) page = 1;
		String msg = ChatColor.GREEN + "Inventories (page " + page + "/" + pages + "): ";
		for(int i = 0; i < 10; i ++) {
			int a = i + (10 * (page - 1));
			if(a >= inventories.size()) break;
			msg += inventories.get(a) + ", ";
		}
		player.sendMessage(msg.substring(0, msg.length() - 2));
	}
	
	public static InventoryManager getInstance() {
		if(instance == null) {
			instance = new InventoryManager();
		}
		return instance;
	}
}
