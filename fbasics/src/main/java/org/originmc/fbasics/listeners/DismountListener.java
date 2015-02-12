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

    private static final BlockFace[] BLOCK_FACES = {
            BlockFace.SELF,
            BlockFace.UP,
            BlockFace.DOWN,
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };
    private static final String PERMISSION_DISMOUNT = "fbasics.bypass.glitch.dismount";
    private final FBasics plugin;
    private final List<Material> hollowMaterials = new ArrayList<>();

    public DismountListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();

        this.plugin = plugin;

        for (String hollowMaterials : materials.getStringList("hollow-materials")) {
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
        }

        if (config.getBoolean("patcher.dismount-glitch")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Dismount-Glitch module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onVehicleExit(VehicleExitEvent event) {
        final Entity entity = event.getExited();

        if (!(entity instanceof Player)) {
            return;
        }

        final Player player = (Player) entity;

        if (player.hasPermission(PERMISSION_DISMOUNT)) {
            return;
        }

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

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.getEntityType().equals(EntityType.HORSE)) {
            return;
        }

        Block block = event.getEntity().getLocation().getBlock();

        for (BlockFace blockFace : BLOCK_FACES) {
            Material material = block.getRelative(blockFace).getType();
            if (!this.hollowMaterials.contains(material)) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
