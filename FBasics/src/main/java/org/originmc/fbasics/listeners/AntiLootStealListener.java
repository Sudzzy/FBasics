package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
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
import org.originmc.fbasics.util.DurationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiLootStealListener implements Listener {

    private static final String PERMISSION_BYPASS = "fbasics.bypass.antiloot";

    private final int protectionDuration;

    private final FBasics plugin;

    private final String msgDropped;

    private final String msgProtected;

    private final Map<UUID, Long> messageCooldowns = new HashMap<>();

    public AntiLootStealListener(FBasics plugin) {
        // Load all settings for the Anti-Loot module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.protectionDuration = config.getInt("anti-looter.protection-duration");
        this.msgDropped = info + language.getString("anti-looter.info.dropped");
        this.msgProtected = info + language.getString("anti-looter.info.protected");

        // Register Anti-Loot events to the server if stated in the config
        if (config.getBoolean("anti-looter.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Anti-Loot module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void denyPickup(PlayerPickupItemEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_BYPASS)) return;

        // Do nothing if loot is not protected
        Item item = event.getItem();
        if (!item.hasMetadata("fbasics-antiloot")) return;

        // Remove metadata from loot if it is no longer being protected or the attacker is picking it up
        String[] itemData = item.getMetadata("fbasics-antiloot").get(0).asString().split("-");
        long remaining = System.currentTimeMillis() - Long.valueOf(itemData[1]);
        if (player.getName().equals(itemData[0]) || remaining >= protectionDuration * 1000) {
            event.getItem().removeMetadata("fbasics-antiloot", plugin);
            return;
        }

        // Prevent the player from picking up the loot
        event.setCancelled(true);

        // Do nothing if player is on a message cooldown
        UUID uuid = player.getUniqueId();
        if (messageCooldowns.containsKey(uuid) &&
                System.currentTimeMillis() - messageCooldowns.get(uuid) < 1000) return;

        // Tell the player this loot is protected
        long cooldown = protectionDuration - remaining / 1000;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgProtected
                .replace("{REMAINING}", DurationUtils.format(cooldown))));

        // Give the player a message cooldown
        messageCooldowns.put(uuid, System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void protectLoot(PlayerDeathEvent event) {
        // Do nothing if the player did not die from PvP
        Player victim = event.getEntity();
        if (victim.getKiller() == null) return;

        // Send killer a confirmation message
        Player killer = victim.getKiller();
        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', msgDropped
                .replace("{TIME}", DurationUtils.format(protectionDuration))));

        // Iterate through each item dropped
        for (ItemStack item : event.getDrops()) {
            // Do nothing if item is not valid
            if (item == null || item.getType() == Material.AIR) continue;

            // Drop this item with the FBasics metadata
            Entity newItem = victim.getWorld().dropItemNaturally(victim.getLocation(), item);
            newItem.setMetadata("fbasics-antiloot",
                    new FixedMetadataValue(plugin, killer.getName() + "-" + System.currentTimeMillis()));
        }

        // Remove all other drops as copies have already been dropped to prevent duplications
        event.getDrops().clear();
    }

}