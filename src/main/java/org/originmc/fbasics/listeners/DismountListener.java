package org.originmc.fbasics.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

public class DismountListener implements Listener {

    private final String permissionDismount = "fbasics.bypass.glitch.dismount";
    private final FBasics plugin;
    private final List<Material> hollowMaterials = new ArrayList<Material>();

    public DismountListener(FBasics plugin) {
        this.plugin = plugin;
        FileConfiguration materials = plugin.getMaterials();

        for (String hollowMaterials : materials.getStringList("hollow-materials"))
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
    }

    @EventHandler(ignoreCancelled = true)
    public void onExit(VehicleExitEvent event) {
        final Entity entity = event.getExited();

        if (!(entity instanceof Player)) return;

        final Player player = (Player) entity;

        if (player.hasPermission(this.permissionDismount)) return;

        if (!this.hollowMaterials.contains(event.getVehicle().getLocation().add(0, 1, 0).getBlock().getType())) {
            final Location location = player.getLocation().subtract(0, 1, 0);

            new BukkitRunnable() {
                @Override
                public void run() {
                    player.teleport(location);
                }
            }.runTask(plugin);
            return;
        }

        final Location location = player.getLocation();

        new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(location);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (!event.getEntityType().equals(EntityType.HORSE)) return;

        Block center = event.getEntity().getLocation().getBlock();
        Block north = center.getRelative(BlockFace.NORTH);
        Block east = center.getRelative(BlockFace.EAST);
        Block south = center.getRelative(BlockFace.SOUTH);
        Block west = center.getRelative(BlockFace.WEST);

        List<Material> surroundingBlockTypes = new ArrayList<Material>();
        surroundingBlockTypes.add(center.getType());
        surroundingBlockTypes.add(north.getType());
        surroundingBlockTypes.add(east.getType());
        surroundingBlockTypes.add(south.getType());
        surroundingBlockTypes.add(west.getType());

        if (!this.hollowMaterials.containsAll(surroundingBlockTypes)) {
            event.setCancelled(true);
        }
    }
}
