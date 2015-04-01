package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;

public class NetherTeleportListener implements Listener {

    private static final String PERMISSION_NETHER = "fbasics.bypass.glitch.nether";

    private final String messageCancelled;

    public NetherTeleportListener(FBasics plugin) {
        // Load all settings for the Nether module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        this.messageCancelled = error + language.getString("patcher.error.nether-cancelled");

        // Register Nether events to the server if stated in the config
        if (config.getBoolean("patcher.nether-glitch")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Nether module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void denyRoofTeleport(PlayerTeleportEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_NETHER)) return;

        // Do nothing if player is not teleporting to the nether
        Location location = event.getTo();
        World.Environment environment = location.getWorld().getEnvironment();
        if (!environment.equals(World.Environment.NETHER)) return;

        // Do nothing if locations Y is lower than 126
        if (location.getY() < 126.0D) return;

        // Deny teleportation
        event.setCancelled(true);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

    @EventHandler(ignoreCancelled = true)
    public void denyDestroy(BlockBreakEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_NETHER)) return;

        // Do nothing if block is lower than the height limit
        if (event.getBlock().getY() < 126) return;

        // Do nothing if block is not in the nether
        if (!event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) return;

        // Deny block edit
        event.setCancelled(true);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_NETHER)) return;

        // Do nothing if block is lower than the height limit
        if (event.getBlock().getY() < 126) return;

        // Do nothing if block is not in the nether
        if (!event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) return;

        // Deny block edit
        event.setCancelled(true);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

}