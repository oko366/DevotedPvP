package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class AcceptCommand extends PlayerCommand {

	public AcceptCommand() {
		super("accept");
		setIdentifier("accept");
		setArguments(1, 1);
		setUsage("/accept <playerName>");
		setDescription("Accepts a duel request");
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
		}
		if (enemy == sender) {
			sender.sendMessage(ChatColor.RED + "You can't accept a duel with yourself!");
			return true;
		}
		DevotedPvP.getInstance().getDuelManager().acceptDuel((Player) sender, enemy);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
