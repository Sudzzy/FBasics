package org.originmc.fbasics.task;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.entity.Tile;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.util.MaterialUtils;
import org.originmc.fbasics.util.PathUtils;

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

            // Do not allow movements larger than 16 blocks distance or there will be serious lag.
            Location current = player.getLocation();
            Location previous = user.getValidLocation();
            if (previous.distanceSquared(current) > 256) {
                player.teleport(previous, PlayerTeleportEvent.TeleportCause.PLUGIN);
                continue;
            }

            // Do nothing if the players' movement is valid.
            if (checkMovement(previous, current, player.isInsideVehicle())) {
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
     * @param user   the players' user profile.
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
     * @param vehicle  if the player is inside a during vehicle this movement.
     * @return true if movement is valid (does not pass through solid blocks).
     */
    public boolean checkMovement(Location previous, Location current, boolean vehicle) {
        // Calculate all possible blocks the player has moved through.
        int moveMinX = Math.min(previous.getBlockX(), current.getBlockX());
        int moveMaxX = Math.max(previous.getBlockX(), current.getBlockX());
        int moveMinY = Math.min(previous.getBlockY(), current.getBlockY());
        int moveMaxY = Math.max(previous.getBlockY(), current.getBlockY()) + 1;
        int moveMinZ = Math.min(previous.getBlockZ(), current.getBlockZ());
        int moveMaxZ = Math.max(previous.getBlockZ(), current.getBlockZ());

        // Increment minimum Y by 1 if the player is currently inside a vehicle to prevent mine cart bugs.
        if (vehicle) moveMinY++;

        // Adjust Y values to the maximum of 256 due to blocks above build limit being solid.
        if (moveMaxY > 256) moveMaxY = 256;
        if (moveMinY > 256) moveMinY = 256;

        // Iterate through all possible blocks passed through.
        boolean passable = true;
        Tile[][][] movement = new Tile[moveMaxX - moveMinX + 1][moveMaxY - moveMinY + 1][moveMaxZ - moveMinZ + 1];
        for (int x = moveMinX; x <= moveMaxX; x++) {
            for (int z = moveMinZ; z <= moveMaxZ; z++) {
                for (int y = moveMinY; y <= moveMaxY; y++) {
                    // Add current blocks' tile to the movement array.
                    int diffX = moveMaxX - x;
                    int diffY = moveMaxY - y;
                    int diffZ = moveMaxZ - z;
                    movement[diffX][diffY][diffZ] = new Tile(diffX, diffY, diffZ);

                    // Do nothing if the material passed through is not full.
                    Block block = previous.getWorld().getBlockAt(x, y, z);
                    if (!MaterialUtils.isFullBlock(block.getType())) continue;

                    // Player has phased if the movement through this block is invalid.
                    if (isPassable(block, previous, current)) {
                        passable = false;
                        movement[diffX][diffY][diffZ].setPassable(false);
                    }
                }
            }
        }

        return passable || PathUtils.hasPath(movement,
                movement[moveMaxX - previous.getBlockX()][moveMaxY - previous.getBlockY() - 1][moveMaxZ - previous.getBlockZ()],
                movement[moveMaxX - current.getBlockX()][moveMaxY - current.getBlockY() - 1][moveMaxZ - current.getBlockZ()]);
    }

    /**
     * Checks if a players' movement could be classed as a phase if moved into a block.
     *
     * @param block    the block to check if the player has phased through.
     * @param previous the first position of movement.
     * @param current  the second position of movement.
     * @return if the players movement could be classed as a phase.
     */
    public boolean isPassable(Block block, Location previous, Location current) {
        // Movement boundaries.
        double moveMaxX = Math.max(previous.getX(), current.getX());
        double moveMinX = Math.min(previous.getX(), current.getX());
        double moveMaxY = Math.max(previous.getY(), current.getY()) + 1.8;
        double moveMinY = Math.min(previous.getY(), current.getY());
        double moveMaxZ = Math.max(previous.getZ(), current.getZ());
        double moveMinZ = Math.min(previous.getZ(), current.getZ());

        // Block boundaries.
        double blockMaxX = block.getLocation().getBlockX() + 1;
        double blockMinX = block.getLocation().getBlockX();
        double blockMaxY = block.getLocation().getBlockY() + 2;
        double blockMinY = block.getLocation().getBlockY();
        double blockMaxZ = block.getLocation().getBlockZ() + 1;
        double blockMinZ = block.getLocation().getBlockZ();

        // Determine whether the player is moving positively in each axis.
        boolean x = previous.getX() < current.getX();
        boolean y = previous.getY() < current.getY();
        boolean z = previous.getZ() < current.getZ();

        return  // Player is within both Y and Z coordinates and has entered through an X face.
                (moveMinX != moveMaxX && moveMinY <= blockMaxY && moveMaxY >= blockMinY && moveMinZ <= blockMaxZ && moveMaxZ >= blockMinZ &&
                        (x && moveMinX <= blockMinX && moveMaxX >= blockMinX || !x && moveMinX <= blockMaxX && moveMaxX >= blockMaxX)) ||

                        // Player is within both X and Z coordinates and has entered through a Y face.
                        (moveMinY != moveMaxY && moveMinX <= blockMaxX && moveMaxX >= blockMinX && moveMinZ <= blockMaxZ && moveMaxZ >= blockMinZ &&
                                (y && moveMinY <= blockMinY && moveMaxY >= blockMinY || !y && moveMinY <= blockMaxY && moveMaxY >= blockMaxY)) ||

                        // Player is within both X and Y coordinates and has entered through a Z face.
                        (moveMinZ != moveMaxZ && moveMinX <= blockMaxX && moveMaxX >= blockMinX && moveMinY <= blockMaxY && moveMaxY >= blockMinY &&
                                (z && moveMinZ <= blockMinZ && moveMaxZ >= blockMinZ || !z && moveMinZ <= blockMaxZ && moveMaxZ >= blockMaxZ));
    }

}
