package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class EloCommand extends PlayerCommand {

	public EloCommand() {
		super("elo");
		setIdentifier("elo");
		setArguments(0, 0);
		setDescription("Check your elo");
		setUsage("/elo");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		DevotedPvP.getInstance().getDuelManager().sendEloMessage((Player) sender);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		return null;
	}

}
