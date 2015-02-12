package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.originmc.fbasics.FBasics;

public class BookLimiterListener implements Listener {

    private static final String PERMISSION_BYPASS = "fbasics.bypass.booklimiter";
    private final FBasics plugin;
    private final int pageLimit;
    private final String playerMessage;

    public BookLimiterListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();

        this.plugin = plugin;
        this.pageLimit = config.getInt("patcher.book-limiter.page-limit");
        this.playerMessage = language.getString("patcher.book-limiter.info.book-too-long");

        if (config.getBoolean("patcher.book-limiter.enabled")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Book-Limiter module loaded");
        }
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (event.getPlayer().hasPermission(PERMISSION_BYPASS)) {
            return;
        }

        if (event.getNewBookMeta().getPageCount() > this.pageLimit) {
            BookMeta newMeta = (BookMeta) this.plugin.getServer().getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
            // Retain old data
            newMeta.setAuthor(event.getPreviousBookMeta().getAuthor());
            newMeta.setTitle(event.getPreviousBookMeta().getTitle());

            newMeta.setPages(newMeta.getPages().subList(0, this.pageLimit - 1));

            event.setNewBookMeta(newMeta);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.playerMessage.replace("{NUMBER}", "" + this.pageLimit)));
        }
    }
}
