package org.originmc.fbasics.cmd;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;

import java.util.List;
import java.util.Random;

public class CmdWilderness implements CommandExecutor {

    private final int xCenter;

    private final int zCenter;

    private final int maxRange;

    private final int minRange;

    private final FBasics plugin;

    private final String messageFailed;

    private final String messageSuccess;

    private final String messageWorld;

    private final String messageConsole;

    private final List<String> worlds;

    private final List<String> blocks;

    public CmdWilderness(FBasics plugin) {
        // Load all the settings for the Wilderness command
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.xCenter = config.getInt("wilderness.x-center");
        this.zCenter = config.getInt("wilderness.z-center");
        this.maxRange = config.getInt("wilderness.max-range");
        this.minRange = config.getInt("wilderness.min-range");
        this.messageFailed = error + language.getString("wilderness.error.failed");
        this.messageSuccess = info + language.getString("wilderness.info.success");
        this.messageWorld = error + language.getString("wilderness.error.world");
        this.messageConsole = error + language.getString("general.error.console");
        this.worlds = config.getStringList("wilderness.whitelisted-worlds");
        this.blocks = config.getStringList("wilderness.disabled-blocks");
        this.plugin = plugin;

        // If config states Wilderness should be enabled, register its command
        if (config.getBoolean("wilderness.enabled")) {
            plugin.getCommand("wilderness").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Do nothing if sender is console
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageConsole));
            return true;
        }

        // Do nothing if wilderness is not enabled in this world
        Player player = (Player) sender;
        World world = player.getWorld();
        if (!worlds.contains(world.getName())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageWorld));
            return true;
        }

        // Attempt to find a safe teleportation location
        for (int counter = 0; counter < 25; counter++) {
            // Generate random set of coordinates
            Random r = new Random();
            int xRange = minRange + r.nextInt(maxRange - minRange);
            int zRange = minRange + r.nextInt(maxRange - minRange);
            int x = xCenter + xRange;
            int z = zCenter + zRange;
            int y = world.getHighestBlockYAt(x, z);

            // Find a different location if the destination is not safe
            Block block = world.getBlockAt(x, y - 1, z);
            if (blocks.contains(block.getType().toString())) {
                continue;
            }

            // Find a different location if the destination is inside claimed factions territory
            if (plugin.getFactionsManager().isInTerritory(block.getLocation())) {
                continue;
            }

            // Teleport player to the destination
            player.teleport(new Location(world, x + 0.5D, y, z + 0.5D));

            // Tell the player the teleportation was a success
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageSuccess
                    .replace("{NAME}", player.getName())
                    .replace("{X}", "" + x)
                    .replace("{Y}", "" + y)
                    .replace("{Z}", "" + z)
                    .replace("{WORLD}", world.getName())));
            return true;
        }

        // Tell player the teleportation has failed
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageFailed));
        return true;
    }

}