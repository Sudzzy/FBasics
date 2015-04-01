package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.originmc.fbasics.FBasics;

public class BoatMovementListener implements Listener {

    private static final String PERMISSION_BOAT = "fbasics.bypass.glitch.boat";

    public BoatMovementListener(FBasics plugin) {
        // Register Boat-Movement events to the server if stated in the config
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("patcher.boat-glitch")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Boat-Glitch module loaded");
        }
    }

    @EventHandler
    public void denyBoatGlitch(PlayerMoveEvent event) {
        // Do nothing if player is not in a vehicle
        Player player = event.getPlayer();
        if (!player.isInsideVehicle()) return;

        // Do nothing if player has permission
        if (player.hasPermission(PERMISSION_BOAT)) return;

        // Do nothing if distance is less than 8
        if (event.getTo().distance(event.getFrom()) < 8D) return;

        // Deny movement
        event.setTo(event.getFrom().setDirection(event.getTo().getDirection()));
    }

}