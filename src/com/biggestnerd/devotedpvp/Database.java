package com.biggestnerd.devotedpvp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceException;

public class Database {
	private String host;
	private int port;
	private String db;
	private String user;
	private String password;
	private Logger logger;
	private Connection connection;
	
	public Database(String host, int port, String db, String user,
			String password, Logger logger) {
		this.host = host;
		this.port = port;
		this.db = db;
		this.user = user;
		this.password = password;
		this.logger = logger;
	}

	public String getDb() {
		return db;
	}

	public String getHost() {
		return host;
	}

	public String getPassword() {
		return password;
	}

	public String getUser() {
		return user;
	}

	public boolean connect() {
		String jdbc = "jdbc:mysql://" + host + ":" + port + "/" + db + "?user="
				+ user + "&password=" + password;
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (Exception ex) {
			throw new DataSourceException("Failed to initialize JDBC drive.");
		}
		try {
			connection = DriverManager.getConnection(jdbc);
			this.logger.log(Level.INFO, "Connected to database!");
			return true;
		} catch (SQLException ex) { // Error handling below:
			this.logger.log(Level.SEVERE,
					"Could not connnect to the database!", ex);
			return false;
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE,
					"An error occured while closing the connection.", ex);
		}
	}

	public boolean isConnected() {
		try {
			return connection.isValid(5);
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "isConnected error!", ex);
		}
		return false;
	}

	public PreparedStatement prepareStatement(String sqlStatement) {
		try {
			return connection.prepareStatement(sqlStatement);
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "Failed to prepare statement! "
					+ sqlStatement, ex);
		}
		return null;
	}

	public void execute(String sql) {
		try {
			if (isConnected()) {
				connection.prepareStatement(sql).executeUpdate();
			} else {
				connect();
				execute(sql);
			}
		} catch (SQLException ex) {
			this.logger.log(Level.SEVERE, "Could not execute SQL statement!",
					ex);
		}
	}
}
