package com.biggestnerd.devotedpvp;

import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;

public class DevotedPvP extends JavaPlugin {

	private static DevotedPvP instance;
	
	private CommandHandler cHandler;
	private Database db;
	private InventoryManager invMan;
	private DuelManager duelMan;
	private MapManager mapMan;
	private World spawnWorld;
	
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
		getServer().getPluginManager().registerEvents(mapMan, this);
		getServer().getPluginManager().registerEvents(duelMan, this);
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
		getCommand("forfeit").setExecutor(cHandler);
		getCommand("wand").setExecutor(cHandler);
		getCommand("structure").setExecutor(cHandler);
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
}
