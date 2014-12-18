package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

public class CropDupeListener implements Listener {


    private final String cropBlock;
    private List<Material> cropBlocks = new ArrayList<Material>();

    public CropDupeListener(FBasics plugin) {
        FileConfiguration materials = plugin.getMaterials();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.cropBlock = error + language.getString("patcher.error.crop-place");

        for (String block : materials.getStringList("block-placement-near-crops"))
            this.cropBlocks.add(Material.getMaterial(block));
    }


    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {

        Action a = e.getAction();

        if (a.equals(Action.LEFT_CLICK_AIR) || a.equals(Action.LEFT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_AIR)) {
            return;
        }

        Player player = e.getPlayer();

        if (!this.cropBlocks.contains(player.getItemInHand().getType())) {
            return;
        }

        BlockFace blockFace = e.getBlockFace();
        Location location = e.getClickedBlock().getLocation();

        if (blockFace.equals(BlockFace.NORTH)) {
            location = location.add(0D, 0D, -1D);
        } else if (blockFace.equals(BlockFace.EAST)) {
            location = location.add(1D, 0D, 0D);
        } else if (blockFace.equals(BlockFace.SOUTH)) {
            location = location.add(0D, 0D, 1D);
        } else if (blockFace.equals(BlockFace.WEST)) {
            location = location.add(-1D, 0D, 0D);
        } else if (blockFace.equals(BlockFace.DOWN)) {
            location = location.add(0D, -1D, 0D);
        } else if (blockFace.equals(BlockFace.UP)) {
            location = location.add(0D, 1D, 0D);
        }

        if (checkBlock(player.getWorld().getBlockAt(location), Material.CACTUS)
                || checkBlock(player.getWorld().getBlockAt(location.add(0D, 1D, 0D)), Material.SUGAR_CANE_BLOCK)
                || player.getWorld().getBlockAt(location).getType().equals(Material.WATER_LILY)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.cropBlock));
            e.setCancelled(true);
        }
    }


    private boolean checkBlock(Block block, Material material) {
        return (block.getRelative(BlockFace.DOWN).getType().equals(material)
                || block.getRelative(BlockFace.NORTH).getType().equals(material)
                || block.getRelative(BlockFace.EAST).getType().equals(material)
                || block.getRelative(BlockFace.SOUTH).getType().equals(material)
                || block.getRelative(BlockFace.WEST).getType().equals(material));
    }
}
