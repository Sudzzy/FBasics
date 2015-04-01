package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

public class AntiPhaseListener implements Listener {

    private static final String PERMISSION_PHASE = "fbasics.bypass.phase";

    private final List<Material> hollowMaterials = new ArrayList<>();

    public AntiPhaseListener(FBasics plugin) {
        // Load all settings for the Anti-Phase module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();

        for (String hollowMaterials : materials.getStringList("hollow-materials")) {
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
        }

        // Register Anti-Phase events to the server if stated in the config
        if (config.getBoolean("patcher.anti-phase")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Anti-Phase module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void denyPhase(PlayerMoveEvent event) {
        // Do nothing if player is flying
        Player player = event.getPlayer();
        if (player.isFlying()) return;

        // Do nothing if player has permission
        if (player.hasPermission(PERMISSION_PHASE)) return;

        // Do nothing if player is going above sky limit
        Location t = event.getTo();
        if (t.getY() > 254) return;

        // Do nothing if player has not moved
        Location f = event.getFrom();
        double distance = f.distance(t);
        if (distance == 0.0D) return;

        // Deny movement if player has moved too far to prevent excessive lookups
        if (distance > 8.0D) {
            event.setTo(f.setDirection(t.getDirection()));
            return;
        }

        // Calculate all possible blocks the player has moved through
        int topBlockX = f.getBlockX() < t.getBlockX() ? t.getBlockX() : f.getBlockX();
        int bottomBlockX = f.getBlockX() > t.getBlockX() ? t.getBlockX() : f.getBlockX();

        int topBlockY = (f.getBlockY() < t.getBlockY() ? t.getBlockY() : f.getBlockY()) + 1;
        int bottomBlockY = f.getBlockY() > t.getBlockY() ? t.getBlockY() : f.getBlockY();

        int topBlockZ = f.getBlockZ() < t.getBlockZ() ? t.getBlockZ() : f.getBlockZ();
        int bottomBlockZ = f.getBlockZ() > t.getBlockZ() ? t.getBlockZ() : f.getBlockZ();

        // Iterate through the outermost coordinates
        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    // Do nothing if material is able to be moved through
                    if (hollowMaterials.contains(f.getWorld().getBlockAt(x, y, z).getType())) continue;

                    // Do nothing if player has walked over stairs
                    if (y == bottomBlockY && f.getBlockY() != t.getBlockY()) continue;

                    // Deny movement
                    event.setTo(f.setDirection(t.getDirection()));
                }
            }
        }
    }

}