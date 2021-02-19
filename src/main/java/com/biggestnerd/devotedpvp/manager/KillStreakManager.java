package com.biggestnerd.devotedpvp.manager;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.bobacadodl.imgmessage.ImageChar;
import com.bobacadodl.imgmessage.ImageMessage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import javax.imageio.ImageIO;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

public class KillStreakManager {

	private HashMap<UUID, Integer> streaks;
	private HashMap<UUID, BufferedImage> faces;
	private Random rng;
	private String[] messageExtras;

	public KillStreakManager() {
		streaks = new HashMap<UUID, Integer>();
		faces = new HashMap<UUID, BufferedImage>();
		rng = new Random();
		if (DevotedPvP.getInstance().getConfig().contains("deathmessages")) {
			messageExtras = DevotedPvP.getInstance().getConfig().getStringList("deathmessages").toArray(new String[0]);
		}
	}

	public int getKillStreak(Player player) {
		if (!streaks.containsKey(player.getUniqueId())) {
			return 0;
		} else {
			return streaks.get(player.getUniqueId());
		}
	}

	public void handlePlayerDeath(Player player) {
		for (PotionEffect effect : player.getActivePotionEffects()) {
			player.removePotionEffect(effect.getType());
		}
		player.getWorld().strikeLightning(player.getLocation().add(0, 100, 0));
		if (faces.containsKey(player.getUniqueId())) {
			BufferedImage face = faces.get(player.getUniqueId());
			int victimPots = countPotions(player.getInventory());
			char heartSymbol = '\u2764';
			String redHeart = "" + ChatColor.RED + heartSymbol + ChatColor.RESET;
			String msg = player.getName() + " (" + victimPots + redHeart + ") was slain.";
			String killermsg = "";
			String extra = messageExtras != null ? messageExtras[rng.nextInt(messageExtras.length)] : "";
			String item = "";
			if (player.getKiller() != null) {
				Player killer = player.getKiller();
				ItemStack weapon = killer.getInventory().getItemInMainHand();
				// If they aren't holding anything, then they used "their bare hands"
				item = weapon.getType() == Material.AIR ? "their bare hands" : weapon.getType().toString();
				// If their weapon has a custom display name, use that instead
				item = weapon.getItemMeta() == null || weapon.getItemMeta().getDisplayName() == null ? item : weapon
						.getItemMeta().getDisplayName();
				msg = player.getName() + " (" + victimPots + redHeart + ") was slain by " + killer.getName();
				// Add the count of potions
				int killerPots = countPotions(killer.getInventory());
				msg += " (" + killerPots + redHeart + ")";

				item = " with " + item + ".";
				incrementKillStreak(killer);
				int streak = getKillStreak(killer);
				killermsg = killer.getName() + " has a killstreak of " + streak;
				if (streak >= 5)
					extra = killer.getName() + " is " + ChatColor.RED + "DOMINATING!";
				if (streak >= 10)
					extra = killer.getName() + "is on a " + ChatColor.RED + ChatColor.BOLD + "RAMPAGE!";
				if (streak >= 15)
					extra = killer.getName() + " is " + ChatColor.AQUA + ChatColor.BOLD + "GODLIKE!";
			}
			resetKillStreak(player);
			new ImageMessage(face, 8, ImageChar.BLOCK.getChar()).appendText("", "", msg, item, killermsg, extra)
					.sendToPlayers(Bukkit.getOnlinePlayers());
		}
	}

	private int countPotions(Inventory inv) {
		int count = 0;

		for (ItemStack item : inv) {
			if (item == null) {
				continue;
			}
			if (item.getType() != Material.POTION && item.getType() != Material.SPLASH_POTION) {
				continue;
			}
			if (!item.hasItemMeta()) {
				continue;
			}
			PotionMeta meta = (PotionMeta) item.getItemMeta();
			assert meta != null;
			if (meta.getBasePotionData().getType() == PotionType.INSTANT_HEAL) {
				count++;
			}
		}

		return count;
	}

	public void incrementKillStreak(Player player) {
		if (!streaks.containsKey(player.getUniqueId())) {
			streaks.put(player.getUniqueId(), 1);
		} else {
			streaks.put(player.getUniqueId(), streaks.get(player.getUniqueId()) + 1);
		}
	}

	public void loadPlayerSkin(Player player) {
		BufferedImage face = null;
		try {
			URL imagePath = new URL("https://www.mc-heads.net/avatar/" + player.getName() + "/8.png");
			face = ImageIO.read(imagePath);
		} catch (IOException e) {
			try {
				face = ImageIO.read(DevotedPvP.getInstance().getResource("_default.png"));
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		if (face != null) {
			faces.put(player.getUniqueId(), face);
		}
	}

	public void resetKillStreak(Player player) {
		streaks.remove(player.getUniqueId());
	}

	public void resetKillStreaks() {
		streaks.clear();
	}
}
