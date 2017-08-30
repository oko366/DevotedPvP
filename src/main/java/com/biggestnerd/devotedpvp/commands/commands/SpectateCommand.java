package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import java.util.List;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class SpectateCommand extends PlayerCommand {

	private RegisteredServiceProvider<Permission> permProvider;

	public SpectateCommand() {
		super("spectate");
		setIdentifier("spectate");
		setArguments(0, 0);
		setUsage("/spectate");
		setDescription("Toggles spectator mode");
		if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			permProvider = Bukkit.getServer().getServicesManager()
					.getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permProvider == null) {
				DevotedPvP.getInstance().warning("Vault is installed, but permission provider could not be loaded");
			}
		}
	}

	@Override
	public boolean execute(CommandSender sender, String[] arg1) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		Player player = (Player) sender;
		boolean takePerms;
		if (player.getGameMode() == GameMode.SPECTATOR) {
			player.setGameMode(Bukkit.getDefaultGameMode());
			takePerms = true;
		} else {
			player.setGameMode(GameMode.SPECTATOR);
			takePerms = false;
		}
		if (permProvider == null) {
			sender.sendMessage(ChatColor.RED
					+ "Vault is not installed, so this command wont properly change permissions");
		} else {
			for (String perm : DevotedPvP.getInstance().getConfigManager().getSpectatorPerms()) {
				if (takePerms) {
					permProvider.getProvider().playerRemove(player, perm);
				} else {
					permProvider.getProvider().playerAdd(player, perm);
				}
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
