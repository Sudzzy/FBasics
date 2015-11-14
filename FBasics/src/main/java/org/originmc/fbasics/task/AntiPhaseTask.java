package org.originmc.fbasics.task;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.User;
import org.originmc.fbasics.util.MaterialUtils;

import java.util.UUID;

@Data
public final class AntiPhaseTask implements Runnable {

    private final FBasics plugin;

    @Override
    public void run() {
        // Iterate through every player.
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Do nothing if the players' current state allows them to bypass this check.
            UUID playerId = player.getUniqueId();
            User user = plugin.getOrCreateUser(playerId);
            if (checkState(player, user)) {
                continue;
            }

            // Do not allow movements larger than 10 blocks distance or there will be serious lag.
            Location current = player.getLocation();
            Location previous = user.getValidLocation();
            if (previous.distance(current) > 10) {
                player.teleport(previous, PlayerTeleportEvent.TeleportCause.PLUGIN);
                continue;
            }

            // Do nothing if the players' movement is valid.
            if (checkMovement(previous, current)) {
                user.setValidLocation(current);
                continue;
            }

            // Teleport the player to the last valid location.
            player.teleport(previous, PlayerTeleportEvent.TeleportCause.UNKNOWN);
        }
    }

    /**
     * Check to see if the players current state can skip a phase check.
     *
     * @param player the player to check for.
     * @param user the players' user profile.
     * @return true if player can skip this current phase check.
     */
    public boolean checkState(Player player, User user) {
        // Do nothing if player has permission.
        if (player.hasPermission(Perm.AntiGlitch.PHASE)) {
            user.setValidLocation(null);
            return true;
        }

        // Do nothing if player is flying.
        if (player.isFlying()) {
            user.setValidLocation(null);
            return true;
        }

        // Do nothing if player has just teleported.
        if (user.isTeleported()) {
            user.setValidLocation(null);
            user.setTeleported(false);
            return true;
        }

        // Do nothing if player does not have a previous valid location.
        Location previous = user.getValidLocation();
        Location current = player.getLocation();
        if (previous == null) {
            user.setValidLocation(current);
            return true;
        }

        // Do nothing if player has changed worlds.
        if (previous.getWorld() != current.getWorld()) {
            user.setValidLocation(null);
            return true;
        }

        // Do nothing if player has not changed block x, y or z.
        if (current.getBlockX() == previous.getBlockX() &&
                current.getBlockY() == previous.getBlockY() &&
                current.getBlockZ() == previous.getBlockZ()) {
            return true;
        }

        // Current player state requires a movement check.
        return false;
    }

    /**
     * Check to see if the movement does not pass through solid blocks.
     *
     * @param previous the first position of movement.
     * @param current  the second position of movement.
     * @return true if movement is valid (does not pass through solid blocks).
     */
    public boolean checkMovement(Location previous, Location current) {
        // Calculate all possible blocks the player has moved through.
        int moveMinX = Math.min(previous.getBlockX(), current.getBlockX());
        int moveMaxX = Math.max(previous.getBlockX(), current.getBlockX());
        int moveMinY = Math.min(previous.getBlockY(), current.getBlockY());
        int moveMaxY = Math.max(previous.getBlockY(), current.getBlockY()) + 1;
        int moveMinZ = Math.min(previous.getBlockZ(), current.getBlockZ());
        int moveMaxZ = Math.max(previous.getBlockZ(), current.getBlockZ());

        // Adjust Y values to the maximum of 256 due to blocks above build limit being solid.
        if (moveMaxY > 256) moveMaxX = 256;
        if (moveMinY > 256) moveMinY = 256;

        // Iterate through all possible blocks passed through.
        for (int x = moveMinX; x <= moveMaxX; x++) {
            for (int z = moveMinZ; z <= moveMaxZ; z++) {
                for (int y = moveMinY; y <= moveMaxY; y++) {
                    // Do nothing if player has walked over stairs and this is the bottommost block.
                    Block block = previous.getWorld().getBlockAt(x, y, z);
                    if (y == moveMinY && previous.getBlockY() != current.getBlockY()) continue;

                    // Deny movement if block is solid.
                    if (MaterialUtils.isFullBlock(block.getType())) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

}
