package com.biggestnerd.devotedpvp;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandHandler implements CommandExecutor {

	private DevotedPvP plugin;
	
	public CommandHandler(DevotedPvP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		switch(label) {
		case "inv": return handleInventoryCommand(sender, args);
		case "queue": return handleQueueCommand(sender);
		case "duel": return handleDuelCommand(sender, args);
		case "accept": return handleAcceptCommand(sender, args);
		case "forfeit": return handleForfeitCommand(sender);
		case "elo": return handleEloCommand(sender);
		case "wand": return handleWandCommand(sender);
		case "structure": return handleStructureCommand(sender, args);
		}
		return true;
	}

	private boolean handleInventoryCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Only players can use the inventory command!");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Invalid arguments, do /inv <load|save|del|transfer> inventory or /inv clear");
			return true;
		}
		Player player = (Player) sender;
		switch(args[0]) {
		case "save":
			if(args.length < 2) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv save inventory");
			} else {
				if(InventoryManager.getInstance().loadInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Inventory successfully loaded: " + args[1]);
				} else {
					if(InventoryManager.getInstance().inventoryExists(args[1])) {
						player.sendMessage(ChatColor.RED + "You can't save an inventory you don't own!");
					} else {
						player.sendMessage(ChatColor.RED + "An error occurred, contact an administrator.");
					}
				}
			}
			break;
		case "load":
			if(args.length < 2) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv load inventory");
			} else {
				if(InventoryManager.getInstance().saveInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Inventory successfully saved: " + args[1]);
				} else {
					player.sendMessage(ChatColor.RED + "Invalid inventory name");
				}
			}
			break;
		case "clear:": 
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			break;
		case "transfer":
			if(args.length < 3) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv transfer inventory newowner");
			} else {
				String inv = args[1];
				Player owner = Bukkit.getPlayer(args[2]);
				if(owner != null) {
					if(InventoryManager.getInstance().transferInventory(player, owner, inv)) {
						player.sendMessage(ChatColor.GREEN + "Ownership of " + inv + " transferred to " + owner.getName());
						if(owner.isOnline()) {
							owner.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " has transferred the inventory " + inv + " to you");
						}
					} else {
						if(InventoryManager.getInstance().inventoryExists(args[1])) {
							player.sendMessage(ChatColor.RED + "You can't transfer an inventory you don't own!");
						} else {
							player.sendMessage(ChatColor.RED + "An error occurred, contact an administrator.");
						}
					}
				} else {
					player.sendMessage(ChatColor.RED + "The specified new owner does not exist, please try again");
				}
			}
			break;
		case "del":
			if(args.length < 2) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv del inventory");
			} else {
				if(InventoryManager.getInstance().deleteInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Successfully deleted inventory " + args[1]);
				} else {
					if(InventoryManager.getInstance().inventoryExists(args[1])) {
						player.sendMessage(ChatColor.RED + "You can't delete an inventory you don't own!");
					} else {
						player.sendMessage(ChatColor.RED + "An error occurred, contact an administrator.");
					}
				}
			}
		}
		return true;
	}

	private boolean handleQueueCommand(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can queue for ranked matches");
			return true;
		}
		plugin.getDuelManager().queuePlayer((Player) sender);
		return true;
	}
	
	private boolean handleDuelCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player))	{
			sender.sendMessage(ChatColor.RED + "Sorry, only players can enter duels");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Invalid arguments, do /duel player");
		} else {
			Player player = (Player) sender;
			Player request = Bukkit.getPlayer(args[0]);
			if(request == null) {
				player.sendMessage(ChatColor.RED + "Player not found, you can't request duels with players who don't exist");
			} else {
				plugin.getDuelManager().requestDuel(player, request);
			}
		}
		return true;
	}
	
	private boolean handleAcceptCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can accept duels");
			return true;
		}
		if(args.length == 0) {
			plugin.getDuelManager().acceptDuel((Player)sender);
		} else {
			Player accepted = Bukkit.getPlayer(args[0]);
			if(accepted == null) {
				sender.sendMessage(ChatColor.RED + "Player not found, you can't accept duels with players who don't exist");
			} else {
				plugin.getDuelManager().acceptDuel((Player)sender, accepted);
			}
		}
		return true;
	}
	
	private boolean handleForfeitCommand(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can forfeit duels");
			return true;
		}
		plugin.getDuelManager().forfeitDuel((Player)sender);
		return true;
	}
	
	private boolean handleEloCommand(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can check their elo");
			return true;
		}
		plugin.getDuelManager().sendEloMessage((Player)sender);
		return true;
	}
	
	private boolean handleWandCommand(CommandSender sender) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can have wands");
			return true;
		}
		plugin.getMapManager().giveWand((Player)sender);
		return true;
	}
	
	private boolean handleStructureCommand(CommandSender sender, String[] args) {
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "Sorry, only players can make structures");
			return true;
		}
		if(args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Invalid arguments, do /structure name");
		} else {
			plugin.getMapManager().makeStructure((Player)sender, args[0]);
		}
		return true;
	}
}