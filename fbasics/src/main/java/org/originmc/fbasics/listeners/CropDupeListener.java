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

    private static final BlockFace[] BLOCK_FACES = {
            BlockFace.NORTH,
            BlockFace.NORTH_EAST,
            BlockFace.EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.NORTH_WEST,
            BlockFace.UP,
            BlockFace.DOWN
    };
    private final String msgCropBlock;
    private final List<Material> dupableBlocks = new ArrayList<>();
    private final List<Material> cropBlocks = new ArrayList<>();

    public CropDupeListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.msgCropBlock = error + language.getString("patcher.error.crop-place");

        for (String block : materials.getStringList("block-placement-near-crops")) {
            this.cropBlocks.add(Material.getMaterial(block));
        }

        for (String block : materials.getStringList("crop-blocks")) {
            this.dupableBlocks.add(Material.getMaterial(block));
        }

        if (config.getBoolean("patcher.crop-dupe")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Crop-Dupe module loaded");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();

        if (!this.cropBlocks.contains(player.getItemInHand().getType())) {
            return;
        }

        Block block = event.getClickedBlock().getRelative(event.getBlockFace());

        for (BlockFace blockFace : BLOCK_FACES) {
            Material material = block.getRelative(blockFace).getType();
            if (this.dupableBlocks.contains(material)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.msgCropBlock));
                player.updateInventory();
                event.setCancelled(true);
                return;
            }
        }
    }
}
