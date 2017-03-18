package com.biggestnerd.devotedpvp;

import java.util.Arrays;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class DevotedPvP extends JavaPlugin {

	private static DevotedPvP instance;
	
	private CommandHandler cHandler;
	private Database db;
	private InventoryManager invMan;
	private DuelManager duelMan;
	private MapManager mapMan;
	private KillStreakManager killMan;
	private World spawnWorld;
	private Location spawnMin;
	private Location spawnMax;
	
	@Override
	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		reloadConfig();
		initializeDb();
		cHandler = new CommandHandler(this);
		registerCommands();
		spawnWorld = getServer().getWorld(getConfig().getString("spawnworld", "world"));
		invMan = InventoryManager.getInstance();
		duelMan = DuelManager.getInstance();
		mapMan = MapManager.getInstance();
		killMan = KillStreakManager.getInstance();
		setupSpawnLocations();
		getServer().getPluginManager().registerEvents(mapMan, this);
		getServer().getPluginManager().registerEvents(duelMan, this);
		getServer().getPluginManager().registerEvents(killMan, this);
	}
	
	@Override
	public void onDisable() {
		MapLoader.cleanUp();
	}
	
	private void initializeDb() {
		ConfigurationSection config = getConfig().getConfigurationSection("sql");
		String host = config.getString("hostname");
		int port = config.getInt("port");
		String dbname = config.getString("dbname");
		String username = config.getString("username");
		String password = config.getString("password");
		db = new Database(host, port, dbname, username, password, getLogger());
		db.connect();
	}
	
	private void registerCommands() {
		getCommand("elo").setExecutor(cHandler);
		getCommand("inv").setExecutor(cHandler);
		getCommand("duel").setExecutor(cHandler);
		getCommand("accept").setExecutor(cHandler);
		getCommand("queue").setExecutor(cHandler);
		PluginCommand ff = getCommand("forfeit");
		ff.setAliases(Arrays.asList(new String[] {"ff", "surrender"}));
		ff.setExecutor(cHandler);
		getCommand("spectate").setExecutor(cHandler);
		getCommand("team").setExecutor(cHandler);
		getCommand("maps").setExecutor(cHandler);
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
	
	public Database getDb() {
		return db;
	}
	
	public InventoryManager getInventoryManager() {
		return invMan;
	}
	
	public DuelManager getDuelManager() {
		return duelMan;
	}
	
	public MapManager getMapManager() {
		return mapMan;
	}
	
	public World getSpawnWorld() {
		return spawnWorld;
	}
	
	public static DevotedPvP getInstance() {
		return instance;
	}
	
	public static void disable() {
		instance.getServer().getPluginManager().disablePlugin(instance);
	}
}
