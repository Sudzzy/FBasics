package org.originmc.fbasics.patches;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.originmc.fbasics.settings.PatchSettings;

public class McMMOPatch implements Listener {

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for(Block block : e.getBlocks()) {
            Material blockMaterial = block.getType();
            if (PatchSettings.mcmmoOres.contains(blockMaterial)) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (e.isSticky() && PatchSettings.mcmmoOres.contains(e.getRetractLocation().getBlock().getType())) {
            e.setCancelled(true);
        }
    }
}
