package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class DuelCommand extends PlayerCommand {

	public DuelCommand() {
		super("duel");
		setIdentifier("duel");
		setArguments(1, 1);
		setDescription("Requests to duel someone");
		setUsage("/duel <playerName>");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		Player enemy = Bukkit.getPlayer(args[0]);
		if (enemy == null) {
			sender.sendMessage(ChatColor.RED + args[0] + " is not online");
			return true;
		}
		DevotedPvP.getInstance().getDuelManager().requestDuel((Player) sender, enemy);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
