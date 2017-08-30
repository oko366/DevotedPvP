package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.manager.DuelManager;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class ForfeitCommand extends PlayerCommand {

	public ForfeitCommand() {
		super("forfeit");
		setIdentifier("forfeit");
		setArguments(0, 0);
		setDescription("Forfeit a duel");
		setUsage("/ff");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		Player p = (Player) sender;
		DuelManager dm = DevotedPvP.getInstance().getDuelManager();
		if (!dm.isInDuel(p.getUniqueId())) {
			p.sendMessage(ChatColor.RED + "You are not in a duel");
			return true;
		}
		dm.forfeitDuel(p);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
