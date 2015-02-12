package org.originmc.fbasics.listeners;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.originmc.fbasics.FBasics;

public class BoatMovementListener implements Listener {

    private static final String PERMISSION_BOAT = "fbasics.bypass.glitch.boat";

    public BoatMovementListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();

        if (config.getBoolean("patcher.boat-glitch")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Boat-Glitch module loaded");
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.isInsideVehicle()
                && !player.hasPermission(PERMISSION_BOAT)
                && event.getTo().distance(event.getFrom()) > 8D) {
            event.setTo(event.getFrom());
        }
    }
}
