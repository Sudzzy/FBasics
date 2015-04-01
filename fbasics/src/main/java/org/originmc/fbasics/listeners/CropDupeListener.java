package org.originmc.fbasics.listeners;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
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

    private static final List<BlockFace> ALL_DIRECTIONS = ImmutableList.of(
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
    );

    private final String msgCropBlock;

    private final List<Material> dupableBlocks = new ArrayList<>();

    private final List<Material> cropBlocks = new ArrayList<>();

    public CropDupeListener(FBasics plugin) {
        // Load all settings for the Crop-Dupe module
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

        // Register Crop-Dupe events to the server if stated in the config
        if (config.getBoolean("patcher.crop-dupe")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Crop-Dupe module loaded");
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void denyCropDupe(PlayerInteractEvent event) {
        // Do nothing if player is right clicking
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        // Do nothing if player is not holding an exploitable material
        Player player = event.getPlayer();
        if (!cropBlocks.contains(player.getItemInHand().getType())) return;

        // Iterate through all blocks surrounding block attempted to be placed
        Block block = event.getClickedBlock().getRelative(event.getBlockFace());
        for (BlockFace blockFace : ALL_DIRECTIONS) {
            // Do nothing if block cannot be duped
            Material material = block.getRelative(blockFace).getType();
            if (!dupableBlocks.contains(material)) continue;

            // Deny placing the block
            player.updateInventory();
            event.setCancelled(true);

            // Send player a message
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgCropBlock));
            return;
        }
    }

}