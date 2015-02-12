package org.originmc.fbasics.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.originmc.fbasics.FBasics;

public class DispenserListener implements Listener {

    public DispenserListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();

        if (config.getBoolean("patcher.dispenser-glitch")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Dispenser-Glitch module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockDispense(BlockDispenseEvent event) {
        event.setCancelled(event.getItem().getType().equals(Material.INK_SACK));
    }

}
