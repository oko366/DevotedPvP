package com.biggestnerd.devotedpvp;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class DuelManager implements Listener {

	private static DuelManager instance;
	
	private Database db;
	private DevotedPvP plugin;
	private QueueThread queueThread;
	
	private HashMap<UUID, UUID> dueling;
	private ConcurrentLinkedQueue<UUID> queue;
	private HashMap<UUID, Integer> eloCache;
	private HashMap<UUID, LinkedList<DuelRequest>> requestedDuels;
	private HashMap<UUID, HashSet<UUID>> spectators;
	private ArrayList<UUID> rankings;
	
	private DuelManager() {
		plugin = DevotedPvP.getInstance();
		db = plugin.getDb();
		setupTables();
		eloCache = new HashMap<UUID, Integer>();
		queue = new ConcurrentLinkedQueue<UUID>();
		dueling = new HashMap<UUID, UUID>();
		rankings = new ArrayList<UUID>();
		requestedDuels = new HashMap<UUID, LinkedList<DuelRequest>>();
		spectators = new HashMap<UUID, HashSet<UUID>>();
		queueThread = new QueueThread();
		queueThread.runTaskTimer(plugin, 1l, 1l);
	}
	 
	private void setupTables() {
		db.execute("CREATE TABLE IF NOT EXISTS elo ("
				+ "player VARCHAR(36) UNIQUE NOT NULL,"
				+ "rank INT)");
	}
	
	public void playerFirstJoin(UUID id) {
		try {
			PreparedStatement joinStatement = db.prepareStatement("INSERT INTO elo (player,rank) VALUES (?,1000)");
			joinStatement.setString(1, id.toString());
			joinStatement.executeUpdate();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void playerWinDuel(Player winner, Player loser) {
		try {
			PreparedStatement getRank = db.prepareStatement("SELECT * FROM elo WHERE player=?");
			getRank.setString(1, winner.getUniqueId().toString());
			ResultSet result = getRank.executeQuery();
			int winRank = 0;
			int loseRank = 0;
			if(result.next()) {
				winRank = result.getInt("rank");
			}
			getRank.setString(1, loser.getUniqueId().toString());
			result = getRank.executeQuery();
			if(result.next()) {
				loseRank = result.getInt("rank");
			}
			if(loseRank != 0 && winRank != 0) {
				PreparedStatement updateRank = db.prepareStatement("UPDATE elo SET rank=? WHERE player=?");
				int e = 1 / (10 - (winRank - loseRank) + 1);
				int newWinElo = winRank + 32 * (1-e);
				updateRank.setInt(1, newWinElo);
				updateRank.setString(2, winner.getUniqueId().toString());
				updateRank.executeUpdate();
				e = 1 / (10 - (loseRank - winRank) + 1);
				int newLoseElo = loseRank + 32 * (-e);
				updateRank.setInt(1, newLoseElo);
				updateRank.setString(2, loser.getUniqueId().toString());
				updateRank.executeUpdate();
				eloCache.put(winner.getUniqueId(), newWinElo);
				eloCache.put(loser.getUniqueId(), newLoseElo);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public int getElo(UUID id) {
		if(!eloCache.containsKey(id)) {
			try {
				PreparedStatement getRank = db.prepareStatement("SELECT * FROM elo WHERE player=?");
				getRank.setString(1, id.toString());
				ResultSet result = getRank.executeQuery();
				if(result.next()) {
					return result.getInt("rank");
				} else {
					playerFirstJoin(id);
					return 1000;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			return eloCache.get(id);
		}
		return 0;
	}
	
	public void fixRank(UUID uuid) {
		rankings.remove(uuid);
		int elo = getElo(uuid);
		ListIterator<UUID> iter = rankings.listIterator();
		while(iter.hasNext()) {
			UUID u = iter.next();
			if(!iter.hasNext()) {
				rankings.add(uuid);
				break;
			}
			if(getElo(u) > elo) {
				iter.previous();
				iter.add(uuid);
				break;
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		eloCache.put(event.getPlayer().getUniqueId(), getElo(event.getPlayer().getUniqueId()));
		fixRank(event.getPlayer().getUniqueId());
		InventoryManager.cleanInventory(event.getPlayer());
		if(event.getPlayer().getWorld().getName().startsWith("duel")) {
			event.getPlayer().teleport(DevotedPvP.getInstance().getSpawnWorld().getSpawnLocation());
		}
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		UUID id = event.getPlayer().getUniqueId();
		eloCache.remove(id);
		queue.remove(id);
		if(dueling.containsKey(id)) {
			boolean ranked = event.getPlayer().getMetadata("ranked").get(0).asBoolean();
			if(ranked) {
				endRankedMatch(dueling.get(id), id);
			} else {
				endDuel(dueling.get(id), id);
			}
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		UUID id = event.getEntity().getUniqueId();
		if(dueling.containsKey(id)) {
			boolean ranked = event.getEntity().getMetadata("ranked").get(0).asBoolean();
			if(ranked) {
				endRankedMatch(dueling.get(id), id);
			} else {
				endDuel(dueling.get(id), id);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerGameModeChangeEvent(PlayerGameModeChangeEvent event) {
		InventoryManager.cleanInventory(event.getPlayer());
	}


	private void endRankedMatch(UUID winner, UUID loser) {
		playerWinDuel(Bukkit.getPlayer(winner), Bukkit.getPlayer(loser));
		fixRank(winner);
		fixRank(loser);
		endDuel(winner, loser);
	}
	
	private void endDuel(UUID winner, UUID loser) {
		if(spectators.containsKey(winner)) {
			for(UUID id : spectators.get(winner)) {
				Player player = Bukkit.getPlayer(id);
				if(player != null) {
					player.teleport(DevotedPvP.getInstance().getSpawnWorld().getSpawnLocation());
					player.setGameMode(GameMode.ADVENTURE);
				}
			}
		}
		if(spectators.containsKey(loser)) {
			for(UUID id : spectators.get(loser)) {
				Player player = Bukkit.getPlayer(id);
				if(player != null) {
					player.teleport(DevotedPvP.getInstance().getSpawnWorld().getSpawnLocation());
					player.setGameMode(GameMode.ADVENTURE);
				}
			}
		}
		dueling.remove(winner);
		dueling.remove(loser);
		Player p1 = Bukkit.getPlayer(winner);
		Player p2 = Bukkit.getPlayer(loser);
		if(p1 != null) {
			p1.teleport(plugin.getSpawnWorld().getSpawnLocation(), TeleportCause.PLUGIN);
			p1.setFireTicks(0);
			p1.sendMessage(ChatColor.GREEN + "You have won your duel against " + p2.getName());
		}
		if(p2 != null) {
			p2.teleport(plugin.getSpawnWorld().getSpawnLocation(), TeleportCause.PLUGIN);
			p2.setFireTicks(0);
			p2.sendMessage(ChatColor.RED + "You have lost your duel against " + p1.getName());
		}
		plugin.getMapManager().handleDuelEnd(winner, loser);
	}
	
	private void startDuel(Player p1, Player p2, boolean ranked) {
		dueling.put(p2.getUniqueId(), p1.getUniqueId());
		dueling.put(p1.getUniqueId(), p2.getUniqueId());
		queue.remove(p1.getUniqueId());
		queue.remove(p2.getUniqueId());
		p1.setMetadata("ranked", new FixedMetadataValue(plugin, ranked));
		p2.setMetadata("ranked", new FixedMetadataValue(plugin, ranked));
		String message = ChatColor.GREEN + "Your" + (ranked ? " ranked" : "") + " duel with %s will begin shortly.";
		p1.sendMessage(String.format(message, p2.getName()));
		p2.sendMessage(String.format(message, p1.getName()));

		InventoryManager.cleanInventory(p1);
		InventoryManager.cleanInventory(p2);

		plugin.getMapManager().randomSpawnForDuel(p1, p2);
	}
	
	public void requestDuel(Player player, Player request, String kitName) {
		if(!requestedDuels.containsKey(request.getUniqueId())) {
			requestedDuels.put(request.getUniqueId(), new LinkedList<DuelRequest>());
		}
		if(requestedDuels.get(request.getUniqueId()).contains(player.getUniqueId())) return;
		if(kitName != null) {
			if(!InventoryManager.getInstance().inventoryExists(kitName)) {
				player.sendMessage(ChatColor.RED + "Inventory does not exist, could not request duel.");
				return;
			}
		}
		requestedDuels.get(request.getUniqueId()).addLast(new DuelRequest(player.getUniqueId(), kitName != null ? kitName : ""));
		String kitMsg = kitName != null ? "kit: " + kitName : "any kit";
		request.sendMessage(ChatColor.GOLD + player.getName() + " has requested a duel with " + kitMsg + ", type /accept " + player.getName() + " to accept the duel.");
		player.sendMessage(ChatColor.GOLD + "You have requested a duel with " + request.getName());
	}
	
	public void acceptDuel(Player player, Player accepted, String kitName) {
		if(isInDuel(accepted.getUniqueId())) {
			player.sendMessage(ChatColor.RED + "That player is in a duel with someone else, please wait to duel them.");
			return;
		}
		if(InventoryManager.getInstance().loadInventory(player, kitName) && InventoryManager.getInstance().loadInventory(accepted, kitName)) {
			startDuel(player, accepted, false);
			requestedDuels.get(player.getUniqueId()).remove(accepted.getUniqueId());
		} else {
			player.sendMessage(ChatColor.RED + "An error occurred starting the duel.");
		}
	}
	
	public void acceptDuel(Player player, Player accepted) {
		if(!requestedDuels.containsKey(player.getUniqueId()) || requestedDuels.get(player.getUniqueId()).contains(accepted)) {
			player.sendMessage(ChatColor.RED + "Nobody has requested a duel with you");
			return;
		}
		int i = requestedDuels.get(player.getUniqueId()).indexOf(accepted.getUniqueId());
		if(i < 0 || i > requestedDuels.get(player.getUniqueId()).size()) return; //should never happen but idk
		DuelRequest req = requestedDuels.get(player.getUniqueId()).get(i);
		acceptDuel(player, accepted, req.kitName);
	}
	
	public void acceptDuel(Player player) {
		if(!requestedDuels.containsKey(player.getUniqueId()) || requestedDuels.get(player.getUniqueId()).isEmpty()) {
			player.sendMessage(ChatColor.RED + "Nobody has requested a duel with you");
			return;
		}
		DuelRequest last = requestedDuels.get(player.getUniqueId()).getLast();
		Player accepted = Bukkit.getPlayer(last.player);
		if(accepted == null) {
			player.sendMessage(ChatColor.RED + "Player not found, could not accept duel.");
			return;
		}
		acceptDuel(player, accepted, last.kitName);
	}
	
	public void forfeitDuel(Player player) {
		if(dueling.containsKey(player.getUniqueId())) {
			boolean ranked = player.getMetadata("ranked").get(0).asBoolean();
			if(ranked) {
				endRankedMatch(dueling.get(player.getUniqueId()), player.getUniqueId());
			} else {
				endDuel(dueling.get(player.getUniqueId()), player.getUniqueId());
			}
		} else {
			player.sendMessage(ChatColor.RED + "You can't forfeit if you're not in a duel!");
		}
	}
	
	public void queuePlayer(Player player) {
		if(queue.contains(player.getUniqueId())) {
			queue.remove(player.getUniqueId());
			player.sendMessage(ChatColor.GREEN + "You have left the ranked queue.");
		} else {
			queue.add(player.getUniqueId());
			player.sendMessage(ChatColor.GREEN + "You have entered the ranked queue.");
		}
	}
	
	public void sendEloMessage(Player player) {
		int rank = 0;
		for(int i = 0; i < rankings.size(); i++) {
			if(rankings.get(i).equals(player.getUniqueId())) {
				rank = i + 1;
				break;
			}
		}
		player.sendMessage(ChatColor.GOLD + "Your elo is " + getElo(player.getUniqueId()) + " and you're ranked #" + rank);
	}
	
	public boolean isInDuel(UUID player) {
		return dueling.containsKey(player);
	}
	
	public void spectatePlayer(Player player, Player spectating) {
		if(!spectators.containsKey(spectating.getUniqueId())) {
			spectators.put(spectating.getUniqueId(), new HashSet<UUID>());
		}
		spectators.get(spectating.getUniqueId()).add(player.getUniqueId());
		player.setGameMode(GameMode.SPECTATOR);
		player.teleport(spectating);
		player.sendMessage(ChatColor.GREEN + "You are now spectating " + spectating.getName());
	}
	
	class QueueThread extends BukkitRunnable {
		
		@Override
		public void run() {
			long start = System.currentTimeMillis();
			while(!queue.isEmpty() && System.currentTimeMillis() - start < 50l) {
				UUID id = queue.poll();
				int high = rankings.indexOf(id);
				int low = high;
				boolean found = false;
				boolean tooLow = false;
				boolean tooHigh= false;
				UUID match = null;
				while(!found) {
					if(tooHigh && tooLow) break;
					if(!tooLow) low--;
					if(!tooHigh) high++;
					if(low > 0) {
						match = rankings.get(low);
						if(queue.contains(match)) {
							found = true;
							break;
						}
					} else {
						tooLow = true;
					}
					if(high < rankings.size()) {
						match = rankings.get(high);
						if(queue.contains(match)) {
							found = true;
							break;
						}
					} else {
						tooHigh = true;
					}
				}
				if(match != null) {
					startDuel(Bukkit.getPlayer(id), Bukkit.getPlayer(match), true);
				}
			}
		}
	}
	
	class DuelRequest {
		UUID player;
		String kitName;
		
		public DuelRequest(UUID player, String kitName) {
			this.player = player;
			this.kitName = kitName;
		}
		
		public int hashCode() {
			return player.hashCode();
		}
		
		public boolean equals(Object o) {
			if(!(o instanceof DuelRequest)) {
				return false;
			}
			DuelRequest other = (DuelRequest)o;
			return other.player.equals(player);
		}
	}
	
	public static DuelManager getInstance() {
		if(instance == null) {
			instance = new DuelManager();
		}
		return instance;
	}
}
