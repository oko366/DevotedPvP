package com.biggestnerd.devotedpvp;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;

import com.bobacadodl.imgmessage.ImageChar;
import com.bobacadodl.imgmessage.ImageMessage;

import net.md_5.bungee.api.ChatColor;

public class KillStreakManager implements Listener {

	private static KillStreakManager instance;
	
	private HashMap<UUID, Integer> streaks;
	private HashMap<UUID, BufferedImage> faces;
	private Random rng;
	private String[] messageExtras;
	
	private KillStreakManager() {
		streaks = new HashMap<UUID, Integer>();
		faces = new HashMap<UUID, BufferedImage>();
		rng = new Random();
		messageExtras = DevotedPvP.getInstance().getConfig().getStringList("deathmessages").toArray(new String[0]);
	}
	
	public int getKillStreak(Player player) {
		if(!streaks.containsKey(player.getUniqueId())) {
			return 0;
		} else {
			return streaks.get(player.getUniqueId());
		}
	}
	
	public void incrementKillStreak(Player player) {
		if(!streaks.containsKey(player.getUniqueId())) {
			streaks.put(player.getUniqueId(), 1);
		} else {
			streaks.put(player.getUniqueId(), streaks.get(player.getUniqueId()) + 1);
		}
	}
	
	public void resetKillStreaks() {
		streaks.clear();
	}
	
	public void resetKillStreak(Player player) {
		streaks.remove(player.getUniqueId());
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		event.setDeathMessage("");
		Player player = event.getEntity();
		for(PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.getWorld().strikeLightning(player.getLocation().add(0, 100, 0));
		if(faces.containsKey(player.getUniqueId())) {
			BufferedImage face = faces.get(player.getUniqueId());
			String msg = player.getName() + " was slain.";
			String killermsg = "";
			String extra = messageExtras[rng.nextInt(messageExtras.length)];
			if(player.getKiller() != null) {
				Player killer = player.getKiller();
				msg = player.getName() + " was slain by " + killer.getName() + ".";
				incrementKillStreak(killer);
				int streak = getKillStreak(killer);
				killermsg = killer.getName() + " has a killstreak of " + streak;
				if(streak >= 5) extra = killer.getName() + " is " + ChatColor.RED + "DOMINATING!";
				if(streak >= 10) extra = killer.getName() + "is one a " + ChatColor.RED + ChatColor.BOLD + "RAMPAGE!";
				if(streak >= 15) extra = killer.getName() + " is " + ChatColor.AQUA + ChatColor.BOLD + "GODLIKE!";
			}
			resetKillStreak(player);
			new ImageMessage(face, 8, ImageChar.BLOCK.getChar()).appendText(
					"",
					"",
					"",
					msg,
					killermsg,
					extra).sendToPlayers(Bukkit.getOnlinePlayers());
		}
	}
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		BufferedImage face = null;
		try {
			URL imagePath = new URL("https://minotar.net/avatar/" + event.getPlayer().getName() + "/8.png");
			face = ImageIO.read(imagePath);
		} catch (IOException e) {
			try {
				face = ImageIO.read(DevotedPvP.getInstance().getResource("_default.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		if(face != null) {
			faces.put(event.getPlayer().getUniqueId(), face);
		}
	}
	
	public static KillStreakManager getInstance() {
		if(instance == null) {
			instance = new KillStreakManager();
		}
		return instance;
	}
}
