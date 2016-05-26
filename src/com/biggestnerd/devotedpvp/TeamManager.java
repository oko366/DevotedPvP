package com.biggestnerd.devotedpvp;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TeamManager {

	private static TeamManager instance;
	
	private HashMap<UUID, ChatColor> teams;
	
	private TeamManager() {
		teams = new HashMap<UUID, ChatColor>();
	}
	
	public void updateNameTag(Player player) {
		if(!teams.containsKey(player)) {
			player.setDisplayName(player.getName());
			return;
		}
		player.setDisplayName(teams.get(player.getUniqueId()).getChar() + player.getName());
	}
	
	public void handleTeamChange(Player player, ChatColor color) {
		teams.put(player.getUniqueId(), color);
		updateNameTag(player);
	}
	
	public ChatColor getTeam(Player player) {
		return teams.get(player.getUniqueId());
	}
	
	public void clearTeam(Player player) {
		teams.remove(player);
		updateNameTag(player);
	}
	
	public static TeamManager getInstance() {
		if(instance == null) {
			instance = new TeamManager();
		}
		return instance;
	}
}
