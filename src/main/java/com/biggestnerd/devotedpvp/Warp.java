package com.biggestnerd.devotedpvp;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Warp {
	private Location first;
	private Location second;
	private String name;

	public Warp(String name) {
		this.name = name;
	}

	public Warp(String name, Location first) {
		this.name = name;
		this.first = first;
	}

	public Warp(String name, Location first, Location second) {
		this.name = name;
		this.first = first;
		this.second = second;
	}

	public Location getFirst() {
		return first;
	}

	public Location getSecond() {
		return second;
	}

	public void setFirst(Location first) {
		this.first = first;
	}

	public void setSecond(Location second) {
		this.second = second;
	}

	public String getName() {
		return name;
	}

	public void tpFirst(Player p) {
		p.teleport(first);
	}

	public void tpSecond(Player p) {
		p.teleport(second);
	}

}
