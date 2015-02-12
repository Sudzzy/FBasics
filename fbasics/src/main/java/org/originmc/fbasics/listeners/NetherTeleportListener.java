package org.originmc.fbasics.listeners;

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
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        this.messageCancelled = error + language.getString("patcher.error.nether-cancelled");

        if (config.getBoolean("patcher.nether-glitch")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Nether module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(PERMISSION_NETHER)) {
            return;
        }

        World world = event.getTo().getWorld();
        Location location = event.getTo();

        if (world.getEnvironment().equals(World.Environment.NETHER) && location.getY() >= 126.0D) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getY() > 125 &&
                !event.getPlayer().hasPermission(PERMISSION_NETHER) &&
                event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getY() > 125 &&
                !event.getPlayer().hasPermission(PERMISSION_NETHER) &&
                event.getBlock().getWorld().getEnvironment().equals(World.Environment.NETHER)) {
            event.setCancelled(true);
        }
    }
}