package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;
import org.originmc.fbasics.util.BukkitUtils;

public final class InventoryDupeListener implements Listener {

    private final AntiGlitchSettings settings;

    public InventoryDupeListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void removeDupedItems(PlayerInteractEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isInventoryDupe()) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.INVENTORY_DUPE)) return;

        if (BukkitUtils.isAtLeast(BukkitUtils.Version.v1_9)) {
            // Validate items in both hands.
            ItemStack offHand = player.getInventory().getItemInOffHand();
            ItemStack mainHand = player.getInventory().getItemInMainHand();

            // Remove item in off hand if invalid size.
            if (offHand != null && offHand.getAmount() <= 0) {
                player.getInventory().setItemInOffHand(null);
            }

            // Remove item in main hand if invalid size.
            if (mainHand != null && mainHand.getAmount() <= 0) {
                player.getInventory().setItemInMainHand(null);
            }
        } else {
            // Remove item in hand if invalid size.
            ItemStack hand = player.getItemInHand();
            if (hand != null && hand.getAmount() <= 0) {
                player.getInventory().setItemInHand(null);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void removeDupedItems(ItemSpawnEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isInventoryDupe()) return;

        // Do nothing if item stack size is greater than 0.
        if (event.getEntity().getItemStack().getAmount() > 0) return;

        // Remove duped item.
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void removeDupedItems(BlockDispenseEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isInventoryDupe()) return;

        // Iterate through the inventory.
        InventoryHolder inventoryHolder = (InventoryHolder) event.getBlock().getState();
        for (ItemStack itemStack : inventoryHolder.getInventory()) {
            // Do nothing if item is null.
            if (itemStack == null) continue;

            // Do nothing if item is air.
            if (itemStack.getType().equals(Material.AIR)) continue;

            // Do nothing if item stack amount is more than 0.
            if (itemStack.getAmount() >= 0) continue;

            // Cancel the event.
            event.setCancelled(true);
        }
    }

}
