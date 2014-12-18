package org.originmc.fbasics.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

public class DismountListener implements Listener {

    private final String permissionDismount = "fbasics.bypass.glitch.dismount";
    private final FBasics plugin;

    public DismountListener(FBasics plugin) {
        this.plugin = plugin;
    }


    @EventHandler(ignoreCancelled = true)
    public void onExit(VehicleExitEvent event) {
        final Entity entity = event.getExited();

        if (!(entity instanceof Player)) {
            return;
        }

        final Location location = event.getVehicle().getLocation();

        if (((Player) entity).hasPermission(this.permissionDismount)) {
            return;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                entity.teleport(location);
            }
        }.runTask(plugin);
    }
}