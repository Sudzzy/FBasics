package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
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
    private final List<Material> dupableBlocks = new ArrayList<Material>();
    private final List<Material> cropBlocks = new ArrayList<Material>();

    public CropDupeListener(FBasics plugin) {
        FileConfiguration materials = plugin.getMaterials();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.cropBlock = error + language.getString("patcher.error.crop-place");

        for (String block : materials.getStringList("block-placement-near-crops"))
            this.cropBlocks.add(Material.getMaterial(block));

        for (String block : materials.getStringList("crop-blocks"))
            this.dupableBlocks.add(Material.getMaterial(block));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        Player player = event.getPlayer();

        if (!this.cropBlocks.contains(player.getItemInHand().getType())) return;

        List<Material> materials = new ArrayList<Material>();
        BlockFace blockFace = event.getBlockFace();
        Block block = event.getClickedBlock().getRelative(blockFace);

        materials.add(block.getRelative(BlockFace.NORTH).getType());
        materials.add(block.getRelative(BlockFace.NORTH_EAST).getType());
        materials.add(block.getRelative(BlockFace.EAST).getType());
        materials.add(block.getRelative(BlockFace.SOUTH_EAST).getType());
        materials.add(block.getRelative(BlockFace.SOUTH).getType());
        materials.add(block.getRelative(BlockFace.SOUTH_WEST).getType());
        materials.add(block.getRelative(BlockFace.WEST).getType());
        materials.add(block.getRelative(BlockFace.NORTH_WEST).getType());
        materials.add(block.getRelative(BlockFace.UP).getType());
        materials.add(block.getRelative(BlockFace.DOWN).getType());

        for (Material material : materials) {
            if (this.dupableBlocks.contains(material)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.cropBlock));
                player.updateInventory();
                event.setCancelled(true);
                return;
            }
        }
    }
}
