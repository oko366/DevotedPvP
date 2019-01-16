package com.biggestnerd.devotedpvp;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles config parsing and holds all values parsed from the config. Runtime config reloading is possible
 *
 */
public class ConfigManager {

	private DevotedPvP plugin;
	private World spawnWorld;
	private PvPDao dao;
	private List<String> spectatorPerms;
	private boolean cleanInventories;

	public ConfigManager(DevotedPvP plugin) {
		this.plugin = plugin;
		reloadConfig();
		parseConfig();
	}

	/**
	 * @return Initialized dao with credentials parsed from the config
	 */
	public PvPDao getDAO() {
		return dao;
	}

	/**
	 * @return World to whichs spawn players will be teleported. May be null if the world specified in the config didnt
	 *         exist
	 */
	public World getSpawnWorld() {
		return spawnWorld;
	}
	
	/**
	 * @return Whether saved inventories should be cleaned of unwanted NBT and unobtainable items
	 */
	public boolean shouldCleanInventories() {
		return cleanInventories;
	}

	/**
	 * @return The permissions temporarily given to players when they go into spectator mode
	 */
	public List<String> getSpectatorPerms() {
		return spectatorPerms;
	}

	/**
	 * Parses the values from the currently loaded plugin config
	 */
	public void parseConfig() {
		FileConfiguration config = plugin.getConfig();
		spawnWorld = Bukkit.getWorld(config.getString("spawnworld", "world"));

		ConfigurationSection dbConfig = config.getConfigurationSection("sql");
		String host = dbConfig.getString("hostname");
		int port = dbConfig.getInt("port");
		String dbname = dbConfig.getString("dbname");
		String username = dbConfig.getString("username");
		String password = dbConfig.getString("password");
		dao = new PvPDao(plugin, username, password, host, port, dbname, 5, 1000L, 600000L, 7200000L);

		spectatorPerms = config.getStringList("spectatorPerms");
		cleanInventories = config.getBoolean("cleanInventories", true);

	}

	/**
	 * Reloads the plugin config from the flat file, but does not parse it
	 */
	public void reloadConfig() {
		plugin.saveDefaultConfig();
		plugin.reloadConfig();
	}

}
