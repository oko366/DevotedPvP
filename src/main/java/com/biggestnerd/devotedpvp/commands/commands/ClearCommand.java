package com.biggestnerd.devotedpvp.commands.commands;

import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class ClearCommand extends PlayerCommand {

	public ClearCommand() {
		super("clear");
		setIdentifier("clear");
		setArguments(0, 0);
		setUsage("/clear");
		setDescription("Clears your inventory");
	}

	@Override
	public boolean execute(CommandSender sender, String[] arg1) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		Player player = (Player) sender;
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		player.sendMessage(ChatColor.GREEN + "Inventory cleared!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		return null;
	}

}
