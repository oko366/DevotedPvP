package com.biggestnerd.devotedpvp;

import com.biggestnerd.devotedpvp.commands.PvPCommandHandler;
import com.biggestnerd.devotedpvp.manager.DuelManager;
import com.biggestnerd.devotedpvp.manager.InventoryManager;
import com.biggestnerd.devotedpvp.manager.KillStreakManager;
import com.biggestnerd.devotedpvp.manager.WarpManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import vg.civcraft.mc.civmodcore.ACivMod;

public class DevotedPvP extends ACivMod {

	private static DevotedPvP instance;

	private PvPDao dao;
	private InventoryManager invMan;
	private DuelManager duelMan;
	private WarpManager warpMan;
	private KillStreakManager killMan;
	private World spawnWorld;
	private Location spawnMin;
	private Location spawnMax;

	@Override
	public void onEnable() {
		instance = this;
		handle = new PvPCommandHandler();
		handle.registerCommands();
		super.onEnable();
		saveDefaultConfig();
		reloadConfig();
		initializeDb();
		spawnWorld = getServer().getWorld(getConfig().getString("spawnworld", "world"));
		invMan = new InventoryManager();
		duelMan = new DuelManager(dao);
		warpMan = new WarpManager();
		killMan = new KillStreakManager();
		setupSpawnLocations();
		getServer().getPluginManager().registerEvents(new PvPListener(duelMan, killMan), this);
	}

	@Override
	public void onDisable() {
		duelMan.saveAllElos();
	}

	private void initializeDb() {
		ConfigurationSection config = getConfig().getConfigurationSection("sql");
		String host = config.getString("hostname");
		int port = config.getInt("port");
		String dbname = config.getString("dbname");
		String username = config.getString("username");
		String password = config.getString("password");
		dao = new PvPDao(this, username, password, host, port, dbname, 5, 1000L, 600000L, 7200000L);
	}

	private void setupSpawnLocations() {
		ConfigurationSection config = getConfig().getConfigurationSection("spawnbounds");
		spawnMin = new Location(spawnWorld, config.getInt("minx"), config.getInt("miny"), config.getInt("minz"));
		spawnMax = new Location(spawnWorld, config.getInt("maxx"), config.getInt("maxy"), config.getInt("maxz"));
	}

	public boolean inSpawn(Player player) {
		Location loc = player.getLocation();
		int x = loc.getBlockX();
		int y = loc.getBlockY();
		int z = loc.getBlockZ();
		return x >= spawnMin.getBlockX() && y >= spawnMin.getBlockY() && z >= spawnMin.getBlockZ()
				&& x <= spawnMax.getBlockX() && y <= spawnMax.getBlockY() && z <= spawnMax.getBlockZ();
	}

	public InventoryManager getInventoryManager() {
		return invMan;
	}

	public DuelManager getDuelManager() {
		return duelMan;
	}

	public PvPDao getDB() {
		return dao;
	}

	public WarpManager getWarpManager() {
		return warpMan;
	}

	public World getSpawnWorld() {
		return spawnWorld;
	}

	public static DevotedPvP getInstance() {
		return instance;
	}

	@Override
	protected String getPluginName() {
		return "DevotedPvP";
	}
}
