package org.originmc.fbasics.listeners;

import org.originmc.fbasics.FBasics;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;

public class NetherTeleportListener implements Listener {

    private final String messageCancelled;
    private final String permissionNether = "fbasics.bypass.glitch.nether";

    public NetherTeleportListener(FBasics plugin) {
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        this.messageCancelled = error + language.getString("patcher.error.nether-cancelled");
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        Player player = e.getPlayer();

        if (player.hasPermission(this.permissionNether)) return;

        World world = e.getTo().getWorld();
        Location location = e.getTo();

        if (world.getEnvironment().equals(World.Environment.NETHER) && location.getY() >= 126.0D) {
            e.setCancelled(true);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }
}