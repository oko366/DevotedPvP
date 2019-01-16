package com.biggestnerd.devotedpvp;

import com.biggestnerd.devotedpvp.commands.PvPCommandHandler;
import com.biggestnerd.devotedpvp.manager.DuelManager;
import com.biggestnerd.devotedpvp.manager.InventoryManager;
import com.biggestnerd.devotedpvp.manager.KillStreakManager;
import com.biggestnerd.devotedpvp.manager.WarpManager;
import vg.civcraft.mc.civmodcore.ACivMod;

public class DevotedPvP extends ACivMod {

	private static DevotedPvP instance;

	public static DevotedPvP getInstance() {
		return instance;
	}
	private PvPDao dao;
	private InventoryManager invMan;
	private DuelManager duelMan;
	private WarpManager warpMan;
	private KillStreakManager killMan;

	private ConfigManager configMan;

	public ConfigManager getConfigManager() {
		return configMan;
	}

	public PvPDao getDB() {
		return dao;
	}

	public DuelManager getDuelManager() {
		return duelMan;
	}

	public InventoryManager getInventoryManager() {
		return invMan;
	}

	@Override
	protected String getPluginName() {
		return "DevotedPvP";
	}

	public WarpManager getWarpManager() {
		return warpMan;
	}

	@Override
	public void onDisable() {
		duelMan.saveAllElos();
		warpMan.saveWarps();
	}

	@Override
	public void onEnable() {
		instance = this;
		handle = new PvPCommandHandler();
		handle.registerCommands();
		super.onEnable();
		configMan = new ConfigManager(this);
		dao = configMan.getDAO();
		invMan = new InventoryManager();
		duelMan = new DuelManager(dao);
		warpMan = new WarpManager();
		killMan = new KillStreakManager();
		getServer().getPluginManager().registerEvents(new PvPListener(duelMan, killMan), this);
	}
}
