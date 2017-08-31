package com.biggestnerd.devotedpvp.manager;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.PvPDao;
import com.biggestnerd.devotedpvp.model.Warp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import vg.civcraft.mc.civmodcore.util.cooldowns.ICoolDownHandler;
import vg.civcraft.mc.civmodcore.util.cooldowns.MilliSecCoolDownHandler;

public class DuelManager {

	private final int AVERAGE_ELO_GAIN = 40;
	private final int STARTING_ELO = 1000;

	private PvPDao db;
	private DevotedPvP plugin;

	private Map<UUID, UUID> dueling;
	private Map<UUID, Integer> eloCache;
	private Map<UUID, Set<UUID>> requestedDuels;
	private ICoolDownHandler<UUID> duelStartInvulCooldowns;

	public DuelManager(PvPDao db) {
		this.db = db;
		plugin = DevotedPvP.getInstance();
		eloCache = db.loadElos();
		dueling = new HashMap<UUID, UUID>();
		requestedDuels = new HashMap<UUID, Set<UUID>>();
		duelStartInvulCooldowns = new MilliSecCoolDownHandler<UUID>(5000);
	}

	public void acceptDuel(Player player, Player accepted) {
		if (!requestedDuels.containsKey(player.getUniqueId())
				|| !requestedDuels.get(player.getUniqueId()).contains(accepted.getUniqueId())) {
			player.sendMessage(ChatColor.RED + accepted.getName() + " has not requested a duel with you");
			return;
		}
		requestedDuels.get(player.getUniqueId()).remove(accepted.getUniqueId());
		startDuel(player, accepted);

	}

	private void adJustElo(UUID winner, UUID loser) {
		int loserElo = getElo(loser);
		int winnerElo = getElo(winner);
		double diff = winnerElo - loserElo;
		double factor = diff / 400;
		double relativeFactor = 1D / (1D + Math.pow(10D, factor));
		int eloDiff = (int) (relativeFactor * AVERAGE_ELO_GAIN);
		eloCache.put(winner, winnerElo + eloDiff);
		Player winnerPlayer = Bukkit.getPlayer(winner);
		if (winnerPlayer != null) {
			winnerPlayer.sendMessage(ChatColor.GOLD + "Your elo is now " + (winnerElo + eloDiff) + " (+" + eloDiff
					+ ")");
		}
		eloCache.put(loser, loserElo - eloDiff);
		Player loserPlayer = Bukkit.getPlayer(loser);
		if (loserPlayer != null) {
			loserPlayer.sendMessage(ChatColor.GOLD + "Your elo is now " + (loserElo - eloDiff) + " (-" + eloDiff + ")");
		}
	}

	public void forfeitDuel(Player player) {
		if (!dueling.containsKey(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "You can't forfeit if you're not in a duel!");
			return;
		}
		playerWinDuel(dueling.get(player.getUniqueId()));
	}

	public int getElo(UUID id) {
		Integer elo = eloCache.get(id);
		if (elo == null) {
			elo = STARTING_ELO;
			eloCache.put(id, elo);
		}
		return elo;
	}

	public Location getLobbyLocation() {
		return plugin.getConfigManager().getSpawnWorld().getSpawnLocation();
	}

	public boolean isInDuel(UUID player) {
		return dueling.containsKey(player);
	}

	public boolean isInvulnerable(Player p) {
		return duelStartInvulCooldowns.onCoolDown(p.getUniqueId());
	}

	public void playerWinDuel(UUID winner) {
		UUID loser = dueling.get(winner);
		adJustElo(winner, loser);
		announceDuelWin(winner, loser);
		dueling.remove(winner);
		dueling.remove(loser);
		Player p1 = Bukkit.getPlayer(winner);
		Player p2 = Bukkit.getPlayer(loser);
		if (p1 != null) {
			p1.teleport(getLobbyLocation(), TeleportCause.PLUGIN);
			p1.setFireTicks(0);
			p1.setHealth(20.0);
			p1.sendMessage(ChatColor.GREEN + "You have won your duel against " + p2.getName());
		}
		if (p2 != null) {
			if (!p2.isDead()) {
				p2.teleport(getLobbyLocation(), TeleportCause.PLUGIN);
				p2.setFireTicks(0);
			}
			p2.sendMessage(ChatColor.RED + "You have lost your duel against " + p1.getName());
		}
		plugin.getWarpManager().handleMatchEnd(p1, p2);
	}

	private void announceDuelWin(UUID winner, UUID loser) {
		Player pWinner = Bukkit.getPlayer(winner);
		Player pLoser = Bukkit.getPlayer(loser);
		String winnerName = pWinner != null ? pWinner.getName() : "unknown";
		String loserName = pLoser != null ? pLoser.getName() : "unknown";
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getUniqueId().equals(winner) || p.getUniqueId().equals(loser)) {
				continue;
			}
			p.sendMessage(ChatColor.DARK_BLUE
					+ String.format("%s (%d) won a duel against %s (%d)", winnerName, getElo(winner), loserName,
							getElo(loser)));
		}
	}

	public void requestDuel(Player player, Player request) {
		if (!requestedDuels.containsKey(request.getUniqueId())) {
			requestedDuels.put(request.getUniqueId(), new HashSet<UUID>());
		}
		if (requestedDuels.get(request.getUniqueId()).contains(player.getUniqueId())) {
			return;
		}
		if (isInDuel(player.getUniqueId())) {
			player.sendMessage(ChatColor.RED
					+ "You are already in a duel with someone else, please finish your current duel");
			return;
		}
		if (isInDuel(request.getUniqueId())) {
			player.sendMessage(ChatColor.RED + request.getName()
					+ " is in a duel with someone else, please wait to duel them.");
			return;
		}
		requestedDuels.get(request.getUniqueId()).add(player.getUniqueId());
		TextComponent acceptMessage = new TextComponent(player.getName()
				+ " has requested a duel with you, run '/accept " + player.getName()
				+ "' or click this message to accept the duel");
		acceptMessage.setColor(net.md_5.bungee.api.ChatColor.GOLD);
		acceptMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/accept " + player.getName()));
		request.spigot().sendMessage(acceptMessage);
		player.sendMessage(ChatColor.GOLD + "You have requested a duel with " + request.getName());
	}

	public void saveAllElos() {
		db.saveElos(eloCache);
	}

	public void sendEloMessage(Player player) {
		player.sendMessage(ChatColor.GOLD + "Your elo is " + getElo(player.getUniqueId()));
	}

	private void startDuel(Player p1, Player p2) {
		dueling.put(p2.getUniqueId(), p1.getUniqueId());
		dueling.put(p1.getUniqueId(), p2.getUniqueId());
		String message = ChatColor.GREEN + "You are now fighting %s";
		p1.sendMessage(String.format(message, p2.getName()));
		p2.sendMessage(String.format(message, p1.getName()));

		InventoryManager.cleanInventory(p1);
		InventoryManager.cleanInventory(p2);
		Warp warp = plugin.getWarpManager().getRandomWarp(p1.getUniqueId());
		if (warp == null) {
			p1.sendMessage(ChatColor.RED + "No arena is available right now, please try again later");
			p2.sendMessage(ChatColor.RED + "No arena is available right now, please try again later");
			return;
		}
		duelStartInvulCooldowns.putOnCoolDown(p1.getUniqueId());
		duelStartInvulCooldowns.putOnCoolDown(p2.getUniqueId());
		p1.teleport(warp.getFirst());
		p2.teleport(warp.getSecond());
	}
}
