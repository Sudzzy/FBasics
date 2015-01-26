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
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AntiLootStealListener implements Listener {

    private final int protectionDuration;
    private final FBasics plugin;
    private final String messageDropped;
    private final String messageProtected;
    private final String messageTimer;
    private final String permissionAntiLooter = "fbasics.bypass.antilooter";
    private List<UUID> messageCooldownPlayers = new ArrayList<UUID>();

    public AntiLootStealListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.protectionDuration = config.getInt("anti-looter.protection-duration");
        this.messageDropped = info + language.getString("anti-looter.info.dropped");
        this.messageProtected = info + language.getString("anti-looter.info.protected");
        this.messageTimer = info + language.getString("anti-looter.info.unprotected");
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent e) {
        Player player = e.getPlayer();
        UUID uuid = player.getUniqueId();
        Item item = e.getItem();
        Boolean meta = item.hasMetadata("fbasics-antiloot");

        if (player.hasPermission(this.permissionAntiLooter) || !meta) return;

        String[] data = item.getMetadata("fbasics-antiloot").get(0).asString().split("-");
        long remaining = System.currentTimeMillis() - Long.valueOf(data[1]);

        if (player.getName().equals(data[0]) || remaining >= this.protectionDuration * 1000) {
            e.getItem().removeMetadata("fbasics-antiloot", plugin);
            return;
        }

        e.setCancelled(true);

        if (this.messageCooldownPlayers.contains(uuid)) return;

        setMessageCooldown(uuid);
        long cooldown = this.protectionDuration - remaining / 1000L;
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageProtected.replace("{REMAINING}", "" + cooldown)));
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (e.getEntity().getKiller() == null) return;

        final Player killer = e.getEntity().getPlayer().getKiller();
        Player victim = e.getEntity().getPlayer();
        killer.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageDropped.replace("{TIME}", "" + this.protectionDuration)));

        new BukkitRunnable() {
            public void run() {
                killer.sendMessage(ChatColor.translateAlternateColorCodes('&', messageTimer));
            }
        }.runTaskLaterAsynchronously(plugin, protectionDuration * 20);

        for (ItemStack a : e.getDrops()) {
            Entity item = victim.getWorld().dropItemNaturally(victim.getLocation(), a);
            item.setMetadata("fbasics-antiloot", new FixedMetadataValue(plugin, killer.getName() + "-" + System.currentTimeMillis()));
        }

        e.getDrops().clear();
    }

    private void setMessageCooldown(final UUID uuid) {
        messageCooldownPlayers.add(uuid);
        new BukkitRunnable() {
            @Override
            public void run() {
                messageCooldownPlayers.remove(uuid);
            }
        }.runTaskLaterAsynchronously(plugin, 20L);
    }
}