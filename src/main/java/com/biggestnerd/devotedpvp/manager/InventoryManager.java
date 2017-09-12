package com.biggestnerd.devotedpvp.manager;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.PvPDao;
import com.biggestnerd.devotedpvp.ItemSafeEvent;
import com.biggestnerd.devotedpvp.model.PvPInventory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.Bukkit;

public class InventoryManager {

	/**
	 * Sanitizes a player's inventory.
	 *
	 * Currently: * Stack Size * Enchantments * Potion effect * Eliminate exploit blocks (bedrock, dragon egg)
	 */
	public static boolean cleanInventory(Player player) {
		try {
			if (player == null)
				return true;
			PlayerInventory inventory = player.getInventory();
			if (inventory == null)
				return true;
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack item = inventory.getItem(i);
				if (item == null)
					continue;
				// Allow external plugins to vouch for an item.
				ItemSafeEvent event = new ItemSafeEvent(item);
				Bukkit.getServer().getPluginManager().callEvent(event);
				if (event.isValid()) continue;
				// done vouch.
				if (Material.BEDROCK.equals(item.getType()) || Material.BARRIER.equals(item.getType())
						|| Material.DRAGON_EGG.equals(item.getType()) || Material.END_CRYSTAL.equals(item.getType())
						|| Material.END_GATEWAY.equals(item.getType()) || Material.ENDER_PORTAL.equals(item.getType())
						|| Material.PORTAL.equals(item.getType()) || Material.STRUCTURE_BLOCK.equals(item.getType())
						|| Material.STRUCTURE_VOID.equals(item.getType())) {
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
					ench.forEach((k, v) -> {
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
	private PvPDao db;
	private DevotedPvP plugin;

	private Map<String, PvPInventory> inventories;

	public InventoryManager() {
		plugin = DevotedPvP.getInstance();
		db = plugin.getDB();
		inventories = new HashMap<String, PvPInventory>();
	}

	public void deleteInventory(PvPInventory inv) {
		inventories.remove(inv.getName().toLowerCase());
		db.deletePvPInventory(inv);
	}

	public PvPInventory getInventory(String name) {
		PvPInventory inv = inventories.get(name.toLowerCase());
		if (inv == null) {
			inv = db.getPvpInventory(name);
			if (inv != null) {
				inventories.put(name.toLowerCase(), inv);
			}
		}
		return inv;
	}

	public Set<String> getInvNames() {
		return inventories.keySet();
	}

	public boolean saveInventory(Player player, String kitName) {
		if (!cleanInventory(player)) {
			plugin.getLogger().log(Level.WARNING, "For some reason, " + player.getName() + " has an uncleanable kit");
			return false;
		}
		if (kitName.length() > 40) {
			kitName = kitName.substring(0, 40);
		}
		PvPInventory inv = PvPInventory.create(kitName, player);
		if (inv == null) {
			return false;
		}
		inventories.put(kitName.toLowerCase(), inv);
		db.savePvPInventory(inv);
		return true;
	}
}
