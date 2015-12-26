package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.settings.AntiGlitchEnderpearlsSettings;
import org.originmc.fbasics.util.DurationUtils;
import org.originmc.fbasics.util.MaterialUtils;
import org.originmc.fbasics.util.MessageUtils;

import java.lang.ref.WeakReference;

public final class EnderpearlListener implements Listener {

    private final FBasics plugin;

    private final AntiGlitchEnderpearlsSettings settings;

    public EnderpearlListener(FBasics plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings().getAntiGlitchSettings().getEnderpearls();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void corretTeleport(PlayerTeleportEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isCorrectTeleport()) return;

        // Do nothing if not teleported by an enderpearl.
        if (!event.getCause().equals(TeleportCause.ENDER_PEARL)) return;

        // Calculate safe teleportation location and change the destination.
        Location to = event.getTo();
        Block block = to.getBlock();
        if (!MaterialUtils.isFullBlock(block.getType())) {
            event.setTo(event.getTo().subtract(0, to.getY() - (int) to.getY(), 0));
        }

        if (MaterialUtils.isFullBlock(block.getRelative(BlockFace.UP).getType())) {
            event.setTo(to.subtract(0, 1, 0));
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyEnderpearlFactions(PlayerTeleportEvent event) {
        // Do nothing if not teleported by an enderpearl.
        if (!event.getCause().equals(TeleportCause.ENDER_PEARL)) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.ENDERPEARLS_FACTION)) return;

        // Do nothing if if player has access to teleport into this territory.
        Location location = event.getTo();
        if (plugin.getFactions().hasAccess(player, location, settings.getFactions(), settings.getFactionsMode())) {
            return;
        }

        // Deny teleportation as it is inside another factions territory.
        event.setCancelled(true);

        // Give player back their enderpearl.
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));

        // Send player a message.
        MessageUtils.sendMessage(player, settings.getFactionsMessage());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void denyEnderpearlBlocks(PlayerInteractEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isDisableWithinBlock()) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.ENDERPEARLS_WITHIN_BLOCK)) return;

        // Do nothing if player is not clicking an enderpearl.
        Material material = player.getItemInHand().getType();
        if (material == null || material != Material.ENDER_PEARL) return;

        // Cancel the event if player clicked a block.
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            return;
        }

        // Do nothing if player did not right click air.
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        // Do nothing if player is not within a solid block.
        Block block = player.getLocation().getBlock();
        if (!MaterialUtils.isFullBlock(block.getRelative(BlockFace.UP).getType()) &&
                !MaterialUtils.isFullBlock(block.getType())) {
            return;
        }

        // Cancel the event and inform the player.
        event.setCancelled(true);
        MessageUtils.sendMessage(player, settings.getDisableWithinBlockMessage());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void denyEnderpearlCooldown(PlayerInteractEvent event) {
        // Do nothing if enderpearl cooldown is invalid.
        if (settings.getCooldown() <= 0) return;

        // Do nothing if player did not right click air.
        if (!event.getAction().equals(Action.RIGHT_CLICK_AIR)) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.ENDERPEARLS_COOLDOWN)) return;

        // Do nothing if player is not clicking an enderpearl.
        Material material = player.getItemInHand().getType();
        if (material == null || material != Material.ENDER_PEARL) return;

        // Deny event if user is already throwing an enderpearl.
        User user = plugin.getOrCreateUser(player.getUniqueId());
        if (user.isThrowingPearl()) {
            event.setCancelled(true);
            MessageUtils.sendMessage(player, settings.getMultipleMessage());
            return;
        }

        // Do nothing if the user is not on a cooldown.
        long enderpearl = DurationUtils.calculateRemaining(user.getEnderpearlCooldown());
        long door = DurationUtils.calculateRemaining(user.getEnderpearlDoorCooldown());
        if (enderpearl < 0 && door < 0) return;

        // Deny the event and send player a message.
        event.setCancelled(true);
        MessageUtils.sendMessage(player, settings.getCooldownMessage()
                .replace("{time}", DurationUtils.format(enderpearl > door ? enderpearl : door)));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void setEnderpearlCooldown(PlayerTeleportEvent event) {
        // Do nothing if not teleported by an enderpearl.
        if (event.getCause() != TeleportCause.ENDER_PEARL) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.ENDERPEARLS_COOLDOWN)) return;

        // Set the users' enderpearl cooldown.
        User user = plugin.getOrCreateUser(player.getUniqueId());
        user.setEnderpearlCooldown(System.currentTimeMillis() + settings.getCooldown());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void setDoorCooldown(PlayerInteractEvent event) {
        // Do nothing if there is no door cooldown.
        if (settings.getDoorCooldown() <= 0) return;

        // Do nothing if player has not right clicked a block.
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.ENDERPEARLS_COOLDOWN)) return;

        // Do nothing if player did not click a door.
        if (!MaterialUtils.isDoorBlock(event.getClickedBlock().getType())) return;

        // Give player a door enderpearl cooldown.
        User user = plugin.getOrCreateUser(player.getUniqueId());
        user.setEnderpearlDoorCooldown(System.currentTimeMillis() + settings.getDoorCooldown());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void setCurrentPearl(ProjectileLaunchEvent event) {
        // Do nothing if entity that died was not an enderpearl.
        if (event.getEntityType() != EntityType.ENDER_PEARL) return;

        // Do nothing if shooter was not a player.
        EnderPearl enderpearl = (EnderPearl) event.getEntity();
        ProjectileSource shooter = enderpearl.getShooter();
        if (!(shooter instanceof Player)) return;

        // Do nothing if user has not thrown an enderpearl.
        User user = plugin.getUsers().get(((Player) shooter).getUniqueId());
        if (user == null) return;

        // User is no longer throwing an enderpearl.
        user.setPearl(new WeakReference<>(enderpearl));
    }

}
