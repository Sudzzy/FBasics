package org.originmc.fbasics.listener;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;
import org.originmc.fbasics.task.TeleportTask;
import org.originmc.fbasics.util.MaterialUtils;

import static org.bukkit.block.BlockFace.*;

public final class DismountListener implements Listener {

    private static final ImmutableSet<BlockFace> SURROUNDING = ImmutableSet.of(
            SELF, NORTH, NORTH_EAST, NORTH_WEST, SOUTH, SOUTH_EAST, SOUTH_WEST, EAST, WEST, UP
    );

    private final FBasics plugin;

    private final AntiGlitchSettings settings;

    public DismountListener(FBasics plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyDismountClipping(VehicleExitEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isDismountClipping()) return;

        // Do nothing if exited was not a player.
        if (!(event.getExited() instanceof Player)) return;

        // Do nothing if player has permission.
        Player player = (Player) event.getExited();
        if (player.hasPermission(Perm.AntiGlitch.DISMOUNT_CLIPPING)) return;

        // Locate a safe position to teleport the player.
        Location location = player.getLocation();
        if (player.getLocation().getY() > 250.0D) {
            location.add(0, 10, 0);
        } else if (!MaterialUtils.isFullBlock(event.getVehicle().getLocation().add(0.0D, 1.0D, 0.0D).getBlock().getType())) {
            location.subtract(0, 1, 0);
        }

        // Teleport player to the safe location on the next tick.
        Bukkit.getScheduler().runTask(plugin, new TeleportTask(player, location));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyDismountClipping(CreatureSpawnEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isDismountClipping()) return;

        // Do nothing if entity is not a horse.
        if (!event.getEntityType().equals(EntityType.HORSE)) return;

        // Cancel event if any surrounding blocks to the spawned horse are solid.
        Block block = event.getEntity().getLocation().getBlock();
        for (BlockFace blockFace : SURROUNDING) {
            if (MaterialUtils.isFullBlock(block.getRelative(blockFace).getType())) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
