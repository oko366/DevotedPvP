package com.biggestnerd.devotedpvp;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import vg.civcraft.mc.civmodcore.ACivMod;
import vg.civcraft.mc.civmodcore.dao.ManagedDatasource;

public class PvPDao extends ManagedDatasource {

	public PvPDao(ACivMod plugin, String user, String pass, String host, int port, String database, int poolSize,
			long connectionTimeout, long idleTimeout, long maxLifetime) {
		super(plugin, user, pass, host, port, database, poolSize, connectionTimeout, idleTimeout, maxLifetime);
		prepareMigrations();
		updateDatabase();
	}

	private void prepareMigrations() {
		registerMigration(0, false, "CREATE TABLE IF NOT EXISTS inventories (name VARCHAR(40) UNIQUE NOT NULL,"
				+ "inv blob NOT NULL,owner VARCHAR(36) NOT NULL, PRIMARY KEY (name));",
				"CREATE TABLE IF NOT EXISTS elos (name VARCHAR(36) UNIQUE NOT NULL, elo INT NOT NULL, PRIMARY KEY (name));");
	}

	public void savePvPInventory(PvPInventory inv) {
		try (Connection conn = getConnection();
				PreparedStatement prep = conn
						.prepareStatement("replace into inventories (name,blob,owner) values(?,?,?);");) {
			prep.setString(1, inv.getName());
			Blob blob = conn.createBlob();
			blob.setBytes(0, inv.getRawData());
			prep.setBlob(2, blob);
			prep.setString(3, inv.getOwner().toString());
			prep.execute();
		} catch (SQLException ex) {
			DevotedPvP.getInstance().warning("Failed to save elo", ex);
		}
	}

	public PvPInventory getPvpInventory(String name) {
		try (Connection conn = getConnection();
				PreparedStatement prep = conn.prepareStatement("SELECT * FROM inventories WHERE name=?;");) {
			prep.setString(1, name);
			try (ResultSet result = prep.executeQuery()) {
				if (result.next()) {
					return new PvPInventory(name, UUID.fromString(result.getString("owner")), result.getBytes("inv"));
				}
			}
		} catch (SQLException ex) {
			DevotedPvP.getInstance().warning("Could not load inv from db", ex);
		}
		return null;
	}

	public void deletePvPInventory(PvPInventory inv) {
		try (Connection conn = getConnection();
				PreparedStatement prep = conn.prepareStatement("delete FROM inventories WHERE name=?;");) {
			prep.setString(1, inv.getName());
			prep.execute();
		} catch (SQLException ex) {
			DevotedPvP.getInstance().warning("Could not delete inv from db", ex);
		}
	}

	public Map<UUID, Integer> loadElos() {
		Map<UUID, Integer> elos = new HashMap<UUID, Integer>();
		try (Connection conn = getConnection();
				PreparedStatement prep = conn.prepareStatement("select name, elo FROM elos;");
				ResultSet rs = prep.executeQuery()) {
			while (rs.next()) {
				String uuidString = rs.getString(1);
				int elo = rs.getInt(2);
				elos.put(UUID.fromString(uuidString), elo);
			}
		} catch (SQLException ex) {
			DevotedPvP.getInstance().warning("Failed to load elo", ex);
		}
		return elos;
	}

	public void saveElos(Map<UUID, Integer> elos) {
		try (Connection conn = getConnection();
				PreparedStatement prep = conn.prepareStatement("replace into elos (name,elo) values(?,?);");) {
			for (Entry<UUID, Integer> entry : elos.entrySet()) {
				prep.setString(1, entry.getKey().toString());
				prep.setInt(2, entry.getValue());
				prep.execute();
			}
		} catch (SQLException ex) {
			DevotedPvP.getInstance().warning("Failed to save elo", ex);
		}
	}

}
