package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.originmc.fbasics.FBasics;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AntiLootStealListener implements Listener {

    private static final String PERMISSION_BYPASS = "fbasics.bypass.antilooter";
    private final int protectionDuration;
    private final FBasics plugin;
    private final String msgDropped;
    private final String msgProtected;
    private final Map<UUID, Long> messageCooldowns = new HashMap<>();

    public AntiLootStealListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.protectionDuration = config.getInt("anti-looter.protection-duration");
        this.msgDropped = info + language.getString("anti-looter.info.dropped");
        this.msgProtected = info + language.getString("anti-looter.info.protected");

        if (config.getBoolean("anti-looter.enabled")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Anti-Looter module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Item item = event.getItem();

        if (player.hasPermission(PERMISSION_BYPASS) || !item.hasMetadata("fbasics-antiloot")) {
            return;
        }

        String[] itemData = item.getMetadata("fbasics-antiloot").get(0).asString().split("-");
        long remaining = System.currentTimeMillis() - Long.valueOf(itemData[1]);

        if (player.getName().equals(itemData[0]) || remaining >= this.protectionDuration * 1000) {
            event.getItem().removeMetadata("fbasics-antiloot", plugin);
            return;
        }

        event.setCancelled(true);

        if (this.messageCooldowns.containsKey(uuid) && System.currentTimeMillis() - this.messageCooldowns.get(uuid) > 1000) {
            return;
        }

        this.messageCooldowns.put(uuid, System.currentTimeMillis());
        long cooldown = this.protectionDuration - remaining / 1000;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.msgProtected.replace("{REMAINING}", "" + cooldown)));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() == null) {
            return;
        }

        Player killer = event.getEntity().getPlayer().getKiller();
        Player victim = event.getEntity().getPlayer();
        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.msgDropped.replace("{TIME}", "" + this.protectionDuration)));

        for (ItemStack itemStack : event.getDrops()) {
            Entity item = victim.getWorld().dropItemNaturally(victim.getLocation(), itemStack);
            item.setMetadata("fbasics-antiloot", new FixedMetadataValue(this.plugin, killer.getName() + "-" + System.currentTimeMillis()));
        }

        event.getDrops().clear();
    }
}
