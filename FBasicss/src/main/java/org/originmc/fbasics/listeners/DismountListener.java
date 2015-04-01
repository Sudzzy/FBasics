package org.originmc.fbasics.listeners;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

public class DismountListener implements Listener {

    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(
            BlockFace.SELF,
            BlockFace.NORTH,
            BlockFace.NORTH_EAST,
            BlockFace.EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.NORTH_WEST,
            BlockFace.UP
    );

    private static final String PERMISSION_DISMOUNT = "fbasics.bypass.glitch.dismount";

    private final FBasics plugin;

    private final List<Material> hollowMaterials = new ArrayList<>();

    public DismountListener(FBasics plugin) {
        // Load all settings for the Dismount-Glitch module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();

        this.plugin = plugin;

        for (String hollowMaterials : materials.getStringList("hollow-materials")) {
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
        }

        // Register Dismount-Glitch events to the server if stated in the config
        if (config.getBoolean("patcher.dismount-glitch")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Dismount-Glitch module loaded");
        }
    }

    private class TeleportTask extends BukkitRunnable {

        private final Player player;

        private final Location location;

        TeleportTask(Player player, Location location) {
            this.player = player;
            this.location = location;
        }

        @Override
        public void run() {
            player.teleport(location);
        }

    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void teleportPlayer(VehicleExitEvent event) {
        // Do nothing if entity is not a player
        if (!(event.getExited() instanceof Player)) return;

        // Do nothing if player has permission
        Player player = (Player) event.getExited();
        if (player.hasPermission(PERMISSION_DISMOUNT)) return;

        // Calculate location depending on what is above the player
        Location location = player.getLocation();
        if (!hollowMaterials.contains(event.getVehicle().getLocation().add(0, 1, 0).getBlock().getType())) {
            location = player.getLocation().subtract(0, 1, 0);
        }

        // Teleport player to safe location on next tick
        new TeleportTask(player, location).runTask(plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void denySpawn(CreatureSpawnEvent event) {
        // Do nothing if entity spawned is not a horse
        if (!event.getEntityType().equals(EntityType.HORSE)) return;

        // Iterate through all blocks surrounding spawn location
        Block block = event.getEntity().getLocation().getBlock();
        for (BlockFace blockFace : ALL_DIRECTIONS) {
            // Do nothing if block is hollow
            Material material = block.getRelative(blockFace).getType();
            if (hollowMaterials.contains(material)) continue;

            // Deny the horse to spawn
            event.setCancelled(true);
            return;
        }
    }

}