package com.biggestnerd.devotedpvp;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class ItemSafeEvent extends Event {
	private static final HandlerList handlers = new HandlerList();

	private boolean valid;
	private final ItemStack item;

	public ItemSafeEvent(final ItemStack item) {
		this.valid = false;
		this.item = item;
	}

	public ItemStack getItem() {
		return item;
	}

	public void setValid() {
		this.valid = true;
	}

	public boolean isValid() {
		return this.valid;
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
}
