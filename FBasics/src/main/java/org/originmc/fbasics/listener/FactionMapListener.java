package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;

public final class FactionMapListener implements Listener {

    private final AntiGlitchSettings settings;

    public FactionMapListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void denyMapExploit(PlayerMoveEvent event) {
        // Do nothing if this module is disabled.
        if (!settings.isFactionMap()) return;

        // Do nothing if player is not in a vehicle.
        Player player = event.getPlayer();
        if (!player.isInsideVehicle()) return;

        // Do nothing if player has permission.
        if (player.hasPermission(Perm.AntiGlitch.FACTION_MAP)) return;

        // Do nothing if distance is less than 8.
        if (event.getTo().distance(event.getFrom()) < 8D) return;

        // Deny movement.
        event.setTo(event.getFrom().setDirection(event.getTo().getDirection()));
    }

}
