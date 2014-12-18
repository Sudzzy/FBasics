package org.originmc.fbasics.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class DispenserListener implements Listener {

    @EventHandler
    public void onDispense(BlockDispenseEvent e) {
        e.setCancelled(e.getItem().getType().equals(Material.INK_SACK));
    }

}
