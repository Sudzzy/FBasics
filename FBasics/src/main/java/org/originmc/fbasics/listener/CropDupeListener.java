package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;
import org.originmc.fbasics.util.MessageUtils;

public final class CropDupeListener implements Listener {

    private static final int radius = 1;

    private final AntiGlitchSettings settings;

    public CropDupeListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void denyCropDupe(PlayerInteractEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isCropDupe()) return;

        // Do nothing if player is right clicking.
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.CROP_DUPE)) return;

        // Do nothing if player is not holding an exploitable material.
        if (!settings.getCropDupeDenyBlocks().contains(player.getItemInHand().getType())) return;

        // Iterate through all blocks surrounding block attempted to be placed.
        Block block = event.getClickedBlock().getRelative(event.getBlockFace());
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    // Do nothing if block cannot be duped.
                    Material material = block.getRelative(x, y, z).getType();
                    if (!settings.getCropDupeCropBlocks().contains(material)) continue;

                    // Deny placing the block.
                    player.updateInventory();
                    event.setCancelled(true);

                    // Send player a message.
                    MessageUtils.sendMessage(player, settings.getCropDupeMessage());
                    return;
                }
            }
        }
    }

}
