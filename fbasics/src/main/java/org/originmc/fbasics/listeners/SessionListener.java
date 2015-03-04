package org.originmc.fbasics.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.CommandEditor;
import org.originmc.fbasics.entity.FBPlayer;

public class SessionListener implements Listener {

    public SessionListener(FBasics plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (FBPlayer.get(player.getUniqueId()) == null) {
            new FBPlayer(player.getUniqueId().toString() + "," + player.getName() + ",0");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        FBPlayer fbPlayer = FBPlayer.get(event.getPlayer().getUniqueId());

        for (CommandEditor commandEditor : fbPlayer.getCooldowns().keySet()) {
            int difference = (int) (System.currentTimeMillis() - fbPlayer.getCooldown(commandEditor)) / 1000;

            if (difference > commandEditor.getCooldown()) {
                fbPlayer.removeCooldown(commandEditor);
            }
        }

        if (fbPlayer.getCooldowns().isEmpty() && fbPlayer.getCrates() == 0) {
            fbPlayer.remove();
        }
    }
}
