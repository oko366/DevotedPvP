package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.manager.WarpManager;
import com.biggestnerd.devotedpvp.model.Warp;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class WarpManageCommand extends PlayerCommand {

	public WarpManageCommand() {
		super("warpManage");
		setIdentifier("wm");
		setArguments(2, 2);
		setDescription("Create or delete warps");
		setUsage("/wm <first|second|delete> name");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		String warpName = args[1];
		Player p = (Player) sender;
		WarpManager wm = DevotedPvP.getInstance().getWarpManager();
		Warp warp = wm.getWarp(warpName);
		switch (args[0]) {
			case "first":
				if (warp == null) {
					wm.addWarp(new Warp(warpName, p.getLocation()));
					p.sendMessage(ChatColor.GREEN + "Created and set first location for warp " + warpName);
				} else {
					warp.setFirst(p.getLocation());
					p.sendMessage(ChatColor.GREEN + "Set first location for warp " + warpName);
				}
				break;
			case "second":
				if (warp == null) {
					wm.addWarp(new Warp(warpName, p.getLocation()));
					p.sendMessage(ChatColor.GREEN + "Created and set first location for warp " + warpName);
					return true;
				} else {
					warp.setSecond(p.getLocation());
					p.sendMessage(ChatColor.GREEN + "Set second location for warp " + warpName);
				}
				break;
			case "delete":
			case "del":
				if (warp == null) {
					p.sendMessage(ChatColor.RED + warpName + " does not exist");
				} else {
					wm.removeWarp(warpName);
					p.sendMessage(ChatColor.GREEN + "Deleted warp " + warpName);
				}
				break;
			default:
				return false;
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String[] arg1) {
		switch (arg1.length) {
			case 0:
				return Arrays.asList(new String[] { "first", "second", "delete" });
			case 1:
				for (String s : new String[] { "first", "second", "delete" }) {
					if (s.startsWith(arg1[0].toLowerCase())) {
						return Arrays.asList(new String[] { s });
					}
				}
			default:
				return null;
		}
	}

}
