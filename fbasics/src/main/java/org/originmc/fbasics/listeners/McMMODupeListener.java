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

    private final List<Material> ores;

    public McMMODupeListener(FBasics plugin) {
        FileConfiguration materials = plugin.getMaterials();
        this.ores = new ArrayList<Material>();
        for (String material : materials.getStringList("ore-blocks"))
            this.ores.add(Material.getMaterial(material));
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for(Block block : e.getBlocks()) {
            Material blockMaterial = block.getType();
            if (this.ores.contains(blockMaterial)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isSticky() && this.ores.contains(e.getRetractLocation().getBlock().getType())) {
            e.setCancelled(true);
        }
    }
}
