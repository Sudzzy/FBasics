package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;

public class ChestDupeListener implements Listener {

    public ChestDupeListener(FBasics plugin) {
        // Load all settings for the Chest-Dupe module
        FileConfiguration config = plugin.getConfig();
        if (config.getBoolean("patcher.chest-dupe")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Chest-Dupe module loaded");
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void removeDupedItems(PlayerInteractEvent event) {
        // Do nothing if item stack size is greater than 0
        Player player = event.getPlayer();
        if (player.getItemInHand().getAmount() > 0) return;

        // Remove duped item
        player.setItemInHand(null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void removeDupedItems(ItemSpawnEvent event) {
        // Do nothing if item stack size is greater than 0
        if (event.getEntity().getItemStack().getAmount() > 0) return;

        // Remove duped item
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void removeDupedItems(BlockDispenseEvent event) {
        // Iterate through the inventory
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        for (ItemStack itemStack : inventoryHolder.getInventory()) {
            // Do nothing if item is null
            if (itemStack == null) continue;

            // Do nothing if item is air
            if (itemStack.getType().equals(Material.AIR)) continue;

            // Do nothing if item stack amount is more than 0
            if (itemStack.getAmount() >= 0) continue;

            // Cancel the event
            event.setCancelled(true);
        }
    }

}