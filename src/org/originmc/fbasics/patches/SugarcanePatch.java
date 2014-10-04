package org.originmc.fbasics.patches;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.PatchSettings;

public class SugarcanePatch implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {

        Action a = e.getAction();

        if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        Player player = e.getPlayer();

        if (!PatchSettings.cactusBlocks.contains(player.getItemInHand().getType())) {
            return;
        }

        Location location = e.getClickedBlock().getLocation().add(0D, 2D, 0D);
        Block block = player.getWorld().getBlockAt(location);

        if (checkBlock(block.getRelative(BlockFace.UP), player)
                || checkBlock(block.getRelative(BlockFace.DOWN), player)
                || checkBlock(block.getRelative(BlockFace.NORTH), player)
                || checkBlock(block.getRelative(BlockFace.SOUTH), player)
                || checkBlock(block.getRelative(BlockFace.EAST), player)
                || checkBlock(block.getRelative(BlockFace.WEST), player)) {
            e.setCancelled(true);
        }
    }


    private boolean checkBlock(Block block, Player player) {

        Material material = block.getType();


        if (!material.equals(Material.SUGAR_CANE_BLOCK)) {
            return false;
        }


        player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cactusBlock));
        return true;
    }
}
