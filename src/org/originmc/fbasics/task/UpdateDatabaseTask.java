package org.originmc.fbasics.task;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class UpdateDatabaseTask extends BukkitRunnable {

    private FBasics plugin;

    public UpdateDatabaseTask(FBasics plugin) {
        this.plugin = plugin;
    }


    @Override
    public void run() {
        for (String name : this.plugin.updateCrates) {
            setCrates(name, this.plugin.crates.get(name));
        }
        this.plugin.updateCrates = new ArrayList<String>();
    }


    public void setCrates(String player, int crates) {

        FileConfiguration config = this.plugin.getConfig();
        boolean mysql = config.getBoolean("mysql.enabled");
        String prefix = config.getString("mysql.table-prefix");

        try {
            if (mysql) {
                PreparedStatement statement = this.plugin.connection.prepareStatement("INSERT INTO `" + prefix + "Crates` (`IGN`, `crates`) VALUES (?, ?) ON DUPLICATE KEY UPDATE `crates` = ?, `IGN` = ?;");
                statement.setString(1, player);
                statement.setInt(2, crates);
                statement.setInt(3, crates);
                statement.setString(4, player);
                statement.executeUpdate();
                statement.close();
            } else {
                player = player.toLowerCase();
                PreparedStatement statement = this.plugin.connection.prepareStatement("INSERT OR IGNORE INTO " + prefix + "Crates (`IGN`, `crates`) VALUES (?, ?);");
                statement.setString(1, player);
                statement.setInt(2, crates);
                statement.executeUpdate();
                statement.close();

                statement = this.plugin.connection.prepareStatement("UPDATE " + prefix + "Crates SET IGN='" + player + "', crates='" + crates + "' WHERE IGN='" + player + "';");
                statement.executeUpdate();
                statement.close();
            }
        } catch (SQLException e) {
            this.plugin.getLogger().severe("Connection to the database failed!");
        }
    }
}
