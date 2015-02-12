package org.originmc.fbasics.task;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SetupDatabaseTask extends BukkitRunnable {

    private FBasics plugin;

    public SetupDatabaseTask(FBasics plugin) {
        this.plugin = plugin;
    }


    @Override
    public void run() {
        FileConfiguration config = this.plugin.getConfig();
        boolean mysql = config.getBoolean("mysql.enabled");
        String user = config.getString("mysql.user-name");
        String pass = config.getString("mysql.user-password");
        String name = config.getString("mysql.database-name");
        String prefix = config.getString("mysql.table-prefix");
        String port = config.getString("mysql.port");
        String host = config.getString("mysql.address");

        this.plugin.getLogger().info("Attempting to connect to the database...");

        try {
            if (mysql) {
                this.plugin.connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true", user, pass);

                if (!tableExists(prefix + "Crates")) {
                    modifyQuery("CREATE TABLE `" + prefix + "Crates` (`IGN` varchar(16) NOT NULL, `crates` int(10) DEFAULT 0, PRIMARY KEY (`IGN`));");
                }

            } else {
                this.plugin.connection = DriverManager.getConnection("jdbc:sqlite://" + this.plugin.getDataFolder().getAbsolutePath() + "//FBasics.db");

                if (!tableExists(prefix + "Crates")) {
                    modifyQuery("CREATE TABLE `" + prefix + "Crates` (`IGN` VARCHAR UNIQUE, `crates` INTEGER DEFAULT 0);");
                }
            }

            ResultSet rs = this.plugin.connection.createStatement().executeQuery("SELECT * FROM " + prefix + "Crates;");
            while (rs.next()) {
                this.plugin.crates.put(rs.getString("IGN"), rs.getInt("crates"));
            }

        } catch (SQLException e) {
            this.plugin.getLogger().severe("Connection to the database failed!");
            return;
        }

        this.plugin.getLogger().info("Connection to the database was a success!");
    }


    private boolean tableExists(String table) throws SQLException {
        ResultSet rs = this.plugin.connection.getMetaData().getTables(null, null, table, null);
        boolean exists = rs.next();
        rs.close();
        return exists;
    }


    private void modifyQuery(String query) throws SQLException {
        Statement statement = this.plugin.connection.createStatement();
        statement.execute(query);
        statement.close();
    }
}
