package org.originmc.fbasics.listeners;

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
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();

        for (String material : materials.getStringList("ore-blocks")) {
            this.ores.add(Material.getMaterial(material));
        }

        if (config.getBoolean("patcher.mcmmo-mining-exploit")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("McMMO module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Material blockMaterial = block.getType();
            if (this.ores.contains(blockMaterial)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        if (event.isSticky() && this.ores.contains(event.getRetractLocation().getBlock().getType())) {
            event.setCancelled(true);
        }
    }
}
