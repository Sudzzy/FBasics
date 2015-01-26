package org.originmc.fbasics.listeners;

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

    private final List<Material> hollowMaterials = new ArrayList<Material>();

    public AntiPhaseListener(FBasics plugin) {
        FileConfiguration materials = plugin.getMaterials();

        for (String hollowMaterials : materials.getStringList("hollow-materials"))
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.isFlying()) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to.getY() > 254) return;

        double distance = from.distance(to);

        if (distance == 0.0D) return;

        if (distance > 5.0D) {
            event.setTo(from);
            return;
        }

        if ((distance > 0.45D) && (!isPathHollow(from, to))) {
            event.setTo(from);
        }
    }

    private boolean isPathHollow(Location loc1, Location loc2) {
        int topBlockX = loc1.getBlockX() < loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();
        int bottomBlockX = loc1.getBlockX() > loc2.getBlockX() ? loc2.getBlockX() : loc1.getBlockX();

        int topBlockY = (loc1.getBlockY() < loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY()) + 1;
        int bottomBlockY = loc1.getBlockY() > loc2.getBlockY() ? loc2.getBlockY() : loc1.getBlockY();

        int topBlockZ = loc1.getBlockZ() < loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();
        int bottomBlockZ = loc1.getBlockZ() > loc2.getBlockZ() ? loc2.getBlockZ() : loc1.getBlockZ();

        for (int x = bottomBlockX; x <= topBlockX; x++) {
            for (int z = bottomBlockZ; z <= topBlockZ; z++) {
                for (int y = bottomBlockY; y <= topBlockY; y++) {
                    if (!this.hollowMaterials.contains(loc1.getWorld().getBlockAt(x, y, z).getType())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
