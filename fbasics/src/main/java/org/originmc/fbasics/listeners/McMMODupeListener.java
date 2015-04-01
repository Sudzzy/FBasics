package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

public class McMMODupeListener implements Listener {

    private final List<Material> ores = new ArrayList<>();

    public McMMODupeListener(FBasics plugin) {
        // Load all settings for the McMMO module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();

        for (String material : materials.getStringList("ore-blocks")) {
            this.ores.add(Material.getMaterial(material));
        }

        // Register McMMO events to the server if stated in the config
        if (config.getBoolean("patcher.mcmmo-mining-exploit")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("McMMO module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void denyPistonGlitch(BlockPistonExtendEvent event) {
        // Iterate through each effected block
        for (Block block : event.getBlocks()) {
            // Do nothing if block is not an ore
            Material blockMaterial = block.getType();
            if (!ores.contains(blockMaterial)) continue;

            // Deny piston from moving
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void denyPistonGlitch(BlockPistonRetractEvent event) {
        // Do nothing if piston is not sticky
        if (!event.isSticky()) return;

        // Do nothing if affected block is not an ore
        @SuppressWarnings("deprecation")
        Material material = event.getRetractLocation().getBlock().getType();
        if (!ores.contains(material)) return;

        // Deny piston from moving
        event.setCancelled(true);
    }

}