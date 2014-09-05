package org.originmc.fbasics.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.settings.DatabaseSettings;

public final class DatabaseManager {

	private FBasics plugin;
	public DatabaseManager(FBasics plugin) {
		this.plugin = plugin;
	}


	public Connection getConnection() {

		if (DatabaseSettings.mysql) {

			try {
				DatabaseSettings.url = "jdbc:mysql://" + DatabaseSettings.host + ":" + DatabaseSettings.port + "/" + DatabaseSettings.name + "?autoReconnect=true";
				Connection connection = DriverManager.getConnection(DatabaseSettings.url, DatabaseSettings.user, DatabaseSettings.pass);
				System.out.println("Database connection success!");
				return connection;
			}

			catch (Exception e) {
				System.out.println("Database connection failed!");
			}

		} else {

			try {
				Connection connection = DriverManager.getConnection(DatabaseSettings.url);
				return connection;
			}

			catch (Exception e) { }
		}

		return null;

	}


	public void setupConnection() {

		if (DatabaseSettings.mysql) {

			try {

				DatabaseSettings.url = "jdbc:mysql://" + DatabaseSettings.host + ":" + DatabaseSettings.port + "?autoReconnect=true";
				Connection connection = DriverManager.getConnection(DatabaseSettings.url, DatabaseSettings.user, DatabaseSettings.pass);
				connection.setCatalog(DatabaseSettings.name);
				connection.close();
				System.out.println("Database connection success!");
				return;

			} catch (Exception e) {
				System.out.println("Database connection failed!");
			}

		} else {

			try {

				DatabaseSettings.url = "jdbc:sqlite://" + this.plugin.getDataFolder().getAbsolutePath() + "//FBasics.db";
				Connection connection = DriverManager.getConnection(DatabaseSettings.url);
				connection.close();
				return;

			} catch (Exception e) { }

		}
	}


	public synchronized int getCrates(String player) {

		Connection connection = getConnection();

		try {

			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM " + DatabaseSettings.prefix + "Crates;");

			while (rs.next()) {

				String ign = rs.getString("IGN");
				int crates = rs.getInt("crates");
				if (ign.equals(player.toLowerCase())) {

					rs.close();
					statement.close();
					connection.close();
					return crates;

				}
			}

		} catch (Exception e) { }

		return 0;

	}


	public synchronized void setCrates(String player, int crates) {

		Connection connection = getConnection();


		if (DatabaseSettings.mysql) {

			try {

				PreparedStatement statement = connection.prepareStatement("INSERT INTO `" + DatabaseSettings.prefix + "Crates` (`IGN`, `crates`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `crates` = ?, `IGN` = ?;");
				statement.setString(1, player);
				statement.setInt(2, crates);
				statement.setInt(3, crates);
				statement.setString(4, player);
				statement.executeUpdate();
				statement.close();

			} catch (Exception e) { }

		} else {

			try {

				player = player.toLowerCase();
				PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO " + DatabaseSettings.prefix + "Crates (`IGN`, `crates`) VALUES (?, ?);");
				statement.setString(1, player);
				statement.setInt(2, crates);
				statement.executeUpdate();
				statement.close();

				statement = connection.prepareStatement("UPDATE " + DatabaseSettings.prefix + "Crates SET IGN='" + player + "', crates='" + crates + "' WHERE IGN='" + player + "';");
				statement.executeUpdate();
				statement.close();
				connection.close();

			} catch (Exception e) { }

		}
	}


	public void createTables() {

		if (DatabaseSettings.mysql) {
			if (!tableExists(DatabaseSettings.prefix + "Crates")) {
				modifyQuery("CREATE TABLE `" + DatabaseSettings.prefix + "Crates` (`IGN` varchar(16) NOT NULL, `crates` int(10) DEFAULT 0, PRIMARY KEY (`IGN`));");
			}

		} else if (!tableExists(DatabaseSettings.prefix + "Crates")) {
			String query = "CREATE TABLE `" + DatabaseSettings.prefix + "Crates` (`IGN` VARCHAR UNIQUE, `crates` INTEGER DEFAULT 0);";
			modifyQuery(query);
		}
	}


	public boolean tableExists(String table) {

		Connection connection = getConnection();

		try {

			ResultSet resultSet = connection.getMetaData().getTables(null, null, table, null);


			if (resultSet.next()) {
				resultSet.close();
				return true;
			}


			resultSet.close();
			connection.close();
			return false;

		} catch (Exception localException) { }

		return false;

	}


	public void modifyQuery(String query) {

		Connection connection = getConnection();

		try {

			Statement statement = connection.createStatement();
			statement.execute(query);
			statement.close();
			connection.close();

		} catch (Exception localException) { }
	}
}