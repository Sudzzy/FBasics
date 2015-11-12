package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.User;
import org.originmc.fbasics.settings.AntiLooterSettings;

import java.util.UUID;

import static org.originmc.fbasics.util.DurationUtils.calculateRemaining;
import static org.originmc.fbasics.util.DurationUtils.format;
import static org.originmc.fbasics.util.MessageUtils.sendMessage;

public final class AntilooterListener implements Listener {

    private static final String META_NAME = "fbasics-antiloot";

    private final FBasics plugin;

    private final AntiLooterSettings settings;

    public AntilooterListener(FBasics plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings().getAntiLooterSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void denyPickup(PlayerPickupItemEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isEnabled()) return;

        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.ANTILOOTER)) return;

        // Do nothing if loot is not protected.
        Item item = event.getItem();
        if (!item.hasMetadata(META_NAME)) return;

        // Remove protection from loot if its protection duration is up.
        UUID playerId = player.getUniqueId();
        String[] itemData = item.getMetadata(META_NAME).get(0).asString().split("\\|");
        long remaining = calculateRemaining(Long.valueOf(itemData[1]));
        if (remaining <= 0 || playerId.toString().equals(itemData[0])) {
            event.getItem().removeMetadata(META_NAME, plugin);
            return;
        }

        // Prevent the player from picking up the loot.
        event.setCancelled(true);

        // Do nothing if player is on a message cooldown.
        User user = plugin.getOrCreateUser(playerId);
        if (calculateRemaining(user.getLooterMessageCooldown()) > 0) return;

        // Tell the player this loot is protected.
        sendMessage(player, settings.getProtectedMessage().replace("{time}", format(remaining)));

        // Give the player a message cooldown.
        user.setLooterMessageCooldown(System.currentTimeMillis() + 1000);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void protectLoot(PlayerDeathEvent event) {
        // Do nothing if this module is not enabled.
        if (!settings.isEnabled()) return;

        // Do nothing if the player did not die from PvP.
        Player victim = event.getEntity();
        if (victim.getKiller() == null) return;

        // Send killer a confirmation message.
        Player killer = victim.getKiller();
        sendMessage(killer, settings.getDroppedMessage().replace("{time}", format(settings.getDuration())));

        // Iterate through each item dropped.
        for (ItemStack item : event.getDrops()) {
            // Do nothing if item is not valid.
            if (item == null || item.getType() == Material.AIR) continue;

            // Drop this item with the FBasics metadata.
            Entity newItem = victim.getWorld().dropItemNaturally(victim.getLocation(), item);
            newItem.setMetadata(META_NAME, new FixedMetadataValue(plugin, killer.getUniqueId().toString() + "|" +
                    (System.currentTimeMillis() + settings.getDuration())));
        }

        // Remove all other drops as copies have already been dropped.
        event.getDrops().clear();
    }

}
