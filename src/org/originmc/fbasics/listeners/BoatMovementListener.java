package org.originmc.fbasics.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BoatMovementListener implements Listener {

    private final String permissionBoat = "fbasics.bypass.glitch.boat";

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        if (player.isInsideVehicle() && !player.hasPermission(this.permissionBoat) && e.getTo().distance(e.getFrom()) > 10.0D) {
            e.setTo(e.getFrom());
        }
    }
}
