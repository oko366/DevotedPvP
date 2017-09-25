package com.biggestnerd.devotedpvp.commands.commands;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.manager.InventoryManager;
import com.biggestnerd.devotedpvp.model.PvPInventory;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.command.PlayerCommand;

public class InvCommand extends PlayerCommand {

	public InvCommand() {
		super("inv");
		setIdentifier("inv");
		setArguments(2, 2);
		setUsage("inv load|save <name>");
		setDescription("Loads or saves an inventory");
	}

	@Override
	public boolean execute(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.MAGIC + "Go away console man");
			return true;
		}
		InventoryManager im = DevotedPvP.getInstance().getInventoryManager();
		Player player = (Player) sender;
		String invName = args[1];
		PvPInventory inv = im.getInventory(invName);
		if (invName.length() > 40) {
			invName = invName.substring(0, 40);
		}
		switch (args[0]) {
			case "save":
				if (inv != null && !inv.getOwner().equals(player.getUniqueId())) {
					player.sendMessage(ChatColor.RED + "An inventory with the name " + invName + " already exists");
					return true;
				}
				if (im.saveInventory(player, invName)) {
					player.sendMessage(ChatColor.GREEN + "Inventory successfully saved: " + args[1]);
				} else {
					player.sendMessage(ChatColor.RED + "An error occurred, contact an administrator.");
				}
				break;
			case "load":
				if (inv == null) {
					player.sendMessage(ChatColor.RED + "The inventory " + invName + " does not exist");
					return true;
				}
				inv.load(player);
				player.sendMessage(ChatColor.GREEN + "Inventory successfully loaded: " + args[1]);
				InventoryManager.cleanInventory(player);
				break;
			case "delete":
			case "del":
				if (inv == null) {
					player.sendMessage(ChatColor.RED + "The inventory " + invName + " does not exist");
					return true;
				}
				if (!inv.getOwner().equals(player.getUniqueId())) {
					player.sendMessage(ChatColor.RED + "You can't delete an inventory you don't own!");
					return true;
				}
				im.deleteInventory(inv);
				player.sendMessage(ChatColor.GREEN + "Successfully deleted inventory " + invName);
				break;
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String[] args) {
		switch (args.length) {
			case 0:
				return Arrays.asList(new String[] { "save", "load", "delete" });
			case 1:
				String arg = args[0].toLowerCase();
				for (String s : new String[] { "save", "load", "delete" }) {
					if (s.startsWith(arg)) {
						return Arrays.asList(new String[] { s });
					}
				}
				return Arrays.asList(new String[] { "save", "load", "delete" });
			case 2:
				String arg2 = args[1].toLowerCase();
				Set<String> invs = DevotedPvP.getInstance().getInventoryManager().getInvNames();
				List<String> result = new LinkedList<String>();
				for (String inv : invs) {
					if (inv.startsWith(arg2)) {
						result.add(inv);
					}
				}
				return result;
		}
		return null;
	}
}
