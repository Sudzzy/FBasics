package org.originmc.fbasics.task;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.User;
import org.originmc.fbasics.settings.WildernessSettings;
import org.originmc.fbasics.settings.WildernessWorldSettings;
import org.originmc.fbasics.util.MessageUtils;

import java.lang.ref.WeakReference;
import java.util.Random;

@Data
public final class WildernessTask implements Runnable {

    private static final Random random = new Random();

    private final int taskId;

    private final FBasics plugin;

    private final User user;

    private final WeakReference<Player> player;

    private final World world;

    private final WildernessSettings settings;

    private final WildernessWorldSettings worldSettings;

    private int searchCount;

    private WildernessFactionsTask factionsTask;

    public WildernessTask(FBasics plugin, Player player, World world, WildernessWorldSettings worldSettings) {
        taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 1, 1).getTaskId();
        user = plugin.getOrCreateUser(player.getUniqueId());
        user.setWildernessTask(this);
        settings = plugin.getSettings().getWildernessSettings();
        this.plugin = plugin;
        this.player = new WeakReference<>(player);
        this.world = world;
        this.worldSettings = worldSettings;
    }

    @Override
    public void run() {
        // Cancel task if the player went offline.
        Player player = this.player.get();
        if (player == null || !player.isOnline()) {
            cancel();
            return;
        }

        if (factionsTask != null) {
            switch (factionsTask.getState()) {
                case SEARCHING:
                    return;
                case ALLOWED:
                    // Teleport player to the destination.
                    int x = factionsTask.getX();
                    int y = factionsTask.getY();
                    int z = factionsTask.getZ();
                    Bukkit.getScheduler().runTask(plugin,
                            new TeleportTask(player, new Location(world, x + 0.5, y + 0.5, z + 0.5)));

                    // Inform the player the teleportation was a success.
                    MessageUtils.sendMessage(player, settings.getSuccessMessage()
                            .replace("{x}", "" + x)
                            .replace("{y}", "" + y)
                            .replace("{z}", "" + z)
                            .replace("{world}", world.getName()));

                    // Stop looking for any new locations.
                    cancel();
                    return;
                case DENIED:
                    factionsTask = null;
            }
        }

        // Cancel task if there have been too many search attempts.
        if (++searchCount > settings.getAttempts()) {
            cancel();
            MessageUtils.sendMessage(player, settings.getAttemptsMessage()
                    .replace("{attempts}", "" + settings.getAttempts()));
            return;
        }

        // Generate a random set of coordinates.
        int x = worldSettings.getCenterX() + random.nextInt(worldSettings.getRange()) - (worldSettings.getRange() / 2);
        int z = worldSettings.getCenterZ() + random.nextInt(worldSettings.getRange()) - (worldSettings.getRange() / 2);
        int y = world.getHighestBlockYAt(x, z);

        // Do nothing if destination is not safe.
        if (!isBlockSafe(world.getBlockAt(x, y, z))) return;

        // Check to see if the location is surrounded by wilderness.
        factionsTask = new WildernessFactionsTask(x, y, z, settings.getFactionsRadius(), world, plugin.getFactions());
        if (settings.isFactionsAsynchronousSearch()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, factionsTask);
        } else {
            Bukkit.getScheduler().runTask(plugin, factionsTask);
        }
    }

    public void cancel() {
        user.setWildernessTask(null);
        Bukkit.getScheduler().cancelTask(taskId);
    }

    private boolean isBlockSafe(Block block) {
        return !settings.getDeniedBlocks().contains(block.getRelative(BlockFace.DOWN).getType()) &&
                block.isEmpty() &&
                block.getRelative(BlockFace.UP).isEmpty();
    }

}
