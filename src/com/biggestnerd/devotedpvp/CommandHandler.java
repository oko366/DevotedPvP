package com.biggestnerd.devotedpvp;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

	private DevotedPvP plugin;
	
	public CommandHandler(DevotedPvP plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if(!(cmd.getName().equals("forfeit") || label.equals("elo") || label.equals("wand") || label.equals("structure")) && !player.hasPermission("pvp.badmin")) {
				if(!plugin.inSpawn(player)) {
					player.sendMessage(ChatColor.RED + "You can only use that command in spawn");
					return true;
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Only players can use that command dummy!");
			return true;
		}
		Player player = (Player) sender;
		switch(label) {
		case "inv": return handleInventoryCommand(player, args);
		case "queue": return handleQueueCommand(player);
		case "duel": return handleDuelCommand(player, args);
		case "accept": return handleAcceptCommand(player, args);
		case "ff":
		case "surrender":
		case "forfeit": return handleForfeitCommand(player);
		case "elo": return handleEloCommand(player);
		case "spectate": return handleSpectateCommand(player, args);
		case "team": return handleTeamCommand(player, args);
		case "maps": return handleMapsCommand(player, args);
		}
		return true;
	}

	private boolean handleInventoryCommand(Player player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv <load|save|del|transfer> inventory or /inv clear");
			return true;
		}
		switch(args[0]) {
		case "save":
			if(args.length < 2) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv save inventory");
			} else {
				if(plugin.getInventoryManager().saveInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Inventory successfully saved: " + args[1]);
				} else {
					if(plugin.getInventoryManager().inventoryExists(args[1])) {
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
				if(plugin.getInventoryManager().loadInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Inventory successfully loaded: " + args[1]);
				} else {
					player.sendMessage(ChatColor.RED + "Invalid inventory name");
				}
			}
			break;
		case "clear": 
			player.getInventory().clear();
			player.getInventory().setArmorContents(null);
			player.sendMessage(ChatColor.GREEN + "Inventory cleared!");
			break;
		case "transfer":
			if(args.length < 3) {
				player.sendMessage(ChatColor.RED + "Invalid arguments, do /inv transfer inventory newowner");
			} else {
				String inv = args[1];
				Player owner = Bukkit.getPlayer(args[2]);
				if(owner != null) {
					if(plugin.getInventoryManager().transferInventory(player, owner, inv)) {
						player.sendMessage(ChatColor.GREEN + "Ownership of " + inv + " transferred to " + owner.getName());
						if(owner.isOnline()) {
							owner.getPlayer().sendMessage(ChatColor.GREEN + player.getName() + " has transferred the inventory " + inv + " to you");
						}
					} else {
						if(plugin.getInventoryManager().inventoryExists(args[1])) {
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
				if(plugin.getInventoryManager().deleteInventory(player, args[1])) {
					player.sendMessage(ChatColor.GREEN + "Successfully deleted inventory " + args[1]);
				} else {
					if(plugin.getInventoryManager().inventoryExists(args[1])) {
						player.sendMessage(ChatColor.RED + "You can't delete an inventory you don't own!");
					} else {
						player.sendMessage(ChatColor.RED + "An error occurred, contact an administrator.");
					}
				}
			}
			break;
		case "list":
			if(!player.hasPermission("pvp.badmin")) {
				player.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			} else {
				int page = 1;
				try {
					page = Integer.parseInt(args[1]);
				} catch (Exception ex) {} //if it's out of bounds or not a number just use 1
				plugin.getInventoryManager().listInventories(player, page);
			}
			break;
		}
		return true;
	}

	private boolean handleQueueCommand(Player player) {
		plugin.getDuelManager().queuePlayer((Player) player);
		return true;
	}
	
	private boolean handleDuelCommand(Player player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(ChatColor.RED + "Invalid arguments, do /duel <player> [inventory]");
		} else {
			if(args[0].equals(player.getName())) {
				player.sendMessage(ChatColor.RED + "You can't request a duel with yourself!");
				return true;
			}
			Player request = Bukkit.getPlayer(args[0]);
			String kitName = null;
			if(args.length > 1) {
				kitName = args[1];
			}
			if(request == null) {
				player.sendMessage(ChatColor.RED + "Player not found, you can't request duels with players who don't exist");
			} else {
				plugin.getDuelManager().requestDuel(player, request, kitName);
			}
		}
		return true;
	}
	
	private boolean handleAcceptCommand(Player player, String[] args) {
		if(args.length == 0) {
			plugin.getDuelManager().acceptDuel((Player)player);
		} else {
			if(args[0].equals(player.getName())) {
				player.sendMessage(ChatColor.RED + "You can't accept a duel with yourself!");
				return true;
			}
			Player accepted = Bukkit.getPlayer(args[0]);
			if(accepted == null) {
				player.sendMessage(ChatColor.RED + "Player not found, you can't accept duels with players who don't exist");
			} else {
				plugin.getDuelManager().acceptDuel(player, accepted);
			}
		}
		return true;
	}
	
	private boolean handleForfeitCommand(Player player) {
		plugin.getDuelManager().forfeitDuel(player);
		return true;
	}
	
	private boolean handleEloCommand(Player player) {
		plugin.getDuelManager().sendEloMessage(player);
		return true;
	}
	
	private boolean handleSpectateCommand(Player player, String[] args) {
		if(args.length == 0) {
			if(player.getGameMode() == GameMode.SPECTATOR) {
				player.sendMessage(ChatColor.GREEN + "You have left spectating mode, teleporting you back to spawn");
				player.teleport(plugin.getSpawnWorld().getSpawnLocation());
				return true;
			}
			player.sendMessage(ChatColor.RED + "Invalid arguments, do /spectate player");
		} else {
			Player other = Bukkit.getPlayer(args[0]);
			if(other == null) {
				player.sendMessage(ChatColor.RED + "Player not found, you can't spectate players who don't exist");
			} else if(plugin.getDuelManager().isInDuel(other.getUniqueId())) {
				plugin.getDuelManager().spectatePlayer(player, other);
			} else {
				player.sendMessage(ChatColor.RED + args[0] + " is not in a duel, you can't spectate players not in a duel");
			}
		}
		return true;
	}
	
	private boolean handleTeamCommand(Player player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(ChatColor.RED + "Invalid arguments, do /team <color|clear>");
		} else {
			String color = args[0].toUpperCase();
			if(color.equalsIgnoreCase("clear")) {
				plugin.getTeamManager().clearTeam(player);
				player.sendMessage(ChatColor.GREEN + "You are not longer on a team");
			} else {
				try {
					ChatColor team = ChatColor.valueOf(color);
					plugin.getTeamManager().handleTeamChange(player, team);
					player.sendMessage("You are now on the " + team + color + ChatColor.WHITE + " team");
				}catch (Exception ex) {
					player.sendMessage(ChatColor.RED + color + " is not a valid team");
				}
			}
		}
		return true;
	}
	
	private boolean handleMapsCommand(Player player, String[] args) {
		if(args.length == 0) {
			player.sendMessage(ChatColor.RED + "Invalid arguments do /maps <list|blank> or /maps create <name>");
		} else {
			switch(args[0]) {
			case "list": plugin.getMapManager().listMaps(player);
				break;
			case "blank": plugin.getMapManager().createBlankMap(player);
				break;
			case "reload": plugin.getMapManager().reload(player);
				break;
			case "del":
				if(args.length < 2) {
					player.sendMessage(ChatColor.RED + "Invalid arguments, do /maps del <name>");
				} else {
					plugin.getMapManager().deleteMap(player, args[1]);
				}
			case "create":
				if(args.length < 2) {
					player.sendMessage(ChatColor.RED + "Invalid arguments, do /maps create <name>");
				} else {
					plugin.getMapManager().createMapLoader(player, args[1]);
				}
				break;
			}
		}
		return true;
	}
}