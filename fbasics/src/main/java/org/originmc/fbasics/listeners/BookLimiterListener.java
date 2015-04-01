package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.originmc.fbasics.FBasics;

public class BookLimiterListener implements Listener {

    private static final String PERMISSION_BYPASS = "fbasics.bypass.booklimiter";

    private final int pageLimit;

    private final String playerMessage;

    public BookLimiterListener(FBasics plugin) {
        // Load all settings for the Book-Limiter module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();

        this.pageLimit = config.getInt("patcher.book-limiter.page-limit");
        this.playerMessage = language.getString("patcher.book-limiter.info.book-too-long");

        // Register Book-Limiter events to the server if stated in the config
        if (config.getBoolean("patcher.book-limiter.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Book-Limiter module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void bookLimiter(PlayerEditBookEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_BYPASS)) return;

        // Do nothing if the page count is smaller or equal to the limit
        if (event.getNewBookMeta().getPageCount() <= pageLimit) return;

        // Retain old book data
        BookMeta newMeta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        newMeta.setAuthor(event.getPreviousBookMeta().getAuthor());
        newMeta.setTitle(event.getPreviousBookMeta().getTitle());

        // Remove any pages in excess of the page limit
        newMeta.setPages(newMeta.getPages().subList(0, pageLimit - 1));
        event.setNewBookMeta(newMeta);

        // Send player a message about the page limitation
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMessage
                .replace("{NUMBER}", "" + pageLimit)));
    }

}