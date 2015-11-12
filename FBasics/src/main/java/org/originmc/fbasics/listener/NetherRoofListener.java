package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;
import org.originmc.fbasics.util.MessageUtils;

public final class NetherRoofListener implements Listener {

    private final AntiGlitchSettings settings;

    public NetherRoofListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyRoofTeleport(PlayerTeleportEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isNetherRoof()) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.NETHER_ROOF)) return;

        // Do nothing if player is not teleporting to the nether.
        Location location = event.getTo();
        if (location.getWorld().getEnvironment() != World.Environment.NETHER) return;

        // Do nothing if location is below height limit.
        if (location.getY() < 126.0D) return;

        // Deny teleportation and send player a message.
        MessageUtils.sendMessage(player, settings.getNetherRoofMessage());
        event.setCancelled(true);

        // Give player back their enderpearl if they used an enderpearl.
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyDestroy(BlockBreakEvent event) {
        if (denyBlockInteract(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyPlace(BlockPlaceEvent event) {
        if (denyBlockInteract(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    private boolean denyBlockInteract(Player player, Block block) {
        // Do nothing if this module is not enabled.
        if (!settings.isNetherRoof()) return false;

        // Do nothing if player has permission.
        if (player.hasPermission(Perm.AntiGlitch.NETHER_ROOF)) return false;

        // Do nothing if block is lower than the height limit.
        if (block.getY() < 126) return false;

        // Do nothing if block is not in the nether.
        if (block.getWorld().getEnvironment() != World.Environment.NETHER) return false;

        // Send player a message.
        MessageUtils.sendMessage(player, settings.getNetherRoofMessage());
        return true;
    }

}
