package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.settings.AntiGlitchSettings;

public final class AntiPhaseListener implements Listener {

    private final FBasics plugin;

    private final AntiGlitchSettings settings;

    public AntiPhaseListener(FBasics plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void setTeleported(PlayerTeleportEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isPhase()) return;

        // Tell plugin player has been teleported in order to allow this movement.
        User user = plugin.getOrCreateUser(event.getPlayer().getUniqueId());
        user.setTeleported(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void setTeleported(PlayerRespawnEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isPhase()) return;

        // Tell plugin player has been teleported in order to allow this movement.
        plugin.getOrCreateUser(event.getPlayer().getUniqueId()).setTeleported(true);
    }

}
