package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;

public class DispenserListener implements Listener {

    public DispenserListener(FBasics plugin) {
        // Register Dispenser-Glitch events to the server if stated in the config
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("patcher.dispenser-glitch")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Dispenser-Glitch module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void denyOPFarms(BlockDispenseEvent event) {
        // Do nothing if item is not inc_sac
        ItemStack itemStack = event.getItem();
        if (!itemStack.getType().equals(Material.INK_SACK)) return;

        // Do nothing if item is not bone meal
        if (itemStack.getDurability() != 15) return;

        // Do nothing if not coming from a dispenser
        if (!event.getBlock().getType().equals(Material.DISPENSER)) return;

        // Deny item being dispensed
        event.setCancelled(true);
    }

}