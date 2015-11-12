package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.settings.AntiGlitchSettings;

public final class DispenserListener implements Listener {

    private final AntiGlitchSettings settings;

    public DispenserListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyBonemealDispensers(BlockDispenseEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isBonemealDispensers()) return;

        // Do nothing if item is not bonemeal.
        ItemStack item = event.getItem();
        if (item.getType() != Material.INK_SACK || item.getDurability() != 15) return;

        // Do nothing if not coming from a dispenser.
        if (event.getBlock().getType() != Material.DISPENSER) return;

        // Cancel the event.
        event.setCancelled(true);
    }

}
