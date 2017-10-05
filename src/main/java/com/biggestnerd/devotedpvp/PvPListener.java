package com.biggestnerd.devotedpvp;

import com.biggestnerd.devotedpvp.manager.DuelManager;
import com.biggestnerd.devotedpvp.manager.InventoryManager;
import com.biggestnerd.devotedpvp.manager.KillStreakManager;
import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PvPListener implements Listener {

	private DuelManager dm;
	private KillStreakManager km;

	public PvPListener(DuelManager dm, KillStreakManager km) {
		this.dm = dm;
		this.km = km;
	}

	@EventHandler
	public void dropItem(PlayerDropItemEvent e) {
		UUID id = e.getPlayer().getUniqueId();
		if (dm.isInDuel(id)) {
			if (e.getItemDrop() != null) {
				e.getItemDrop().remove();
			}
		}
	}

	private void giveItem(ItemFrame frame, Player p) {
		ItemStack item = frame.getItem();
		if (item == null) {
			return;
		}
		item.setAmount(item.getType().getMaxStackSize());
		p.getInventory().addItem(item);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void hitItemFrame(EntityDamageByEntityEvent e) {
		if (e.getEntity() instanceof ItemFrame) {
			e.setCancelled(true);
		} else {
			return;
		}
		if (!(e.getDamager() instanceof Player)) {
			return;
		}
		Player p = (Player) e.getDamager();
		if (p.hasPermission("civpvp.badmin")) {
			return;
		}
		ItemFrame frame = (ItemFrame) e.getEntity();
		giveItem(frame, p);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void interactItemFrame(PlayerInteractEntityEvent e) {
		if (e.getPlayer().hasPermission("civpvp.badmin")) {
			return;
		}
		Entity ent = e.getRightClicked();
		if (!(ent instanceof ItemFrame)) {
			return;
		}
		ItemFrame frame = (ItemFrame) ent;
		giveItem(frame, e.getPlayer());
		e.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		UUID id = event.getEntity().getUniqueId();
		if (dm.isInDuel(id)) {
			dm.forfeitDuel(event.getEntity());
			event.getDrops().clear();
		} else {
			km.handlePlayerDeath(event.getEntity());
		}
		event.setDeathMessage("");

	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
		InventoryManager.cleanInventory(event.getPlayer());
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		InventoryManager.cleanInventory(event.getPlayer());
		event.getPlayer().teleport(dm.getLobbyLocation());
		km.loadPlayerSkin(event.getPlayer());
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		UUID id = event.getPlayer().getUniqueId();
		if (dm.isInDuel(id)) {
			dm.forfeitDuel(event.getPlayer());
		}
	}

	@EventHandler
	public void playerDamage(EntityDamageEvent e) {
		if (e.getEntityType() != EntityType.PLAYER) {
			return;
		}
		if (dm.isInvulnerable((Player) e.getEntity())) {
			e.setCancelled(true);
		}
	}

}
