package com.biggestnerd.devotedpvp.model;

import com.biggestnerd.devotedpvp.DevotedPvP;
import com.biggestnerd.devotedpvp.manager.InventoryManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PvPInventory {

	public static PvPInventory create(String name, Player p) {
		InventoryManager.cleanInventory(p);
		CraftPlayer craft = (CraftPlayer) p;
		EntityHuman human = craft.getHandle();
		NBTTagCompound nbt = new NBTTagCompound();
		human.save(nbt);
		NBTTagCompound invNBT = new NBTTagCompound();
		invNBT.set("inventory", nbt.get("Inventory"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			NBTCompressedStreamTools.a(invNBT, out);
		} catch (IOException e) {
			DevotedPvP.getInstance().getLogger().log(Level.WARNING, "Failed to compress inventory", e);
			p.sendMessage(ChatColor.RED + "Failed to save inventory");
			return null;
		}
		return new PvPInventory(name, p.getUniqueId(), out.toByteArray());
	}

	private byte[] data;
	private UUID owner;

	private String name;

	public PvPInventory(String name, UUID owner, byte[] data) {
		this.name = name;
		this.owner = owner;
		this.data = data;
	}

	public String getName() {
		return name;
	}

	public UUID getOwner() {
		return owner;
	}

	public byte[] getRawData() {
		return data;
	}

	public boolean isOwner(UUID comp) {
		return owner.equals(comp);
	}

	public void load(Player p) {
		ByteArrayInputStream input = new ByteArrayInputStream(data);
		NBTTagCompound nbt;
		try {
			nbt = NBTCompressedStreamTools.a(input);
		} catch (IOException e) {
			DevotedPvP.getInstance().getLogger().log(Level.WARNING, "Failed to decompress inventory", e);
			p.sendMessage(ChatColor.RED + "Failed to load inventory");
			return;
		}
		if (nbt == null) {
			return;
		}
		CraftPlayer craft = (CraftPlayer) p;
		EntityPlayer human = craft.getHandle();
		NBTTagCompound parent = new NBTTagCompound();
		human.save(parent);
		parent.set("Inventory", nbt.get("inventory"));
		human.f(parent);
	}
}
