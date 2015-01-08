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

    private final FBasics plugin;
    private final int pageLimit;
    private final String PERMISSION_BYPASS = "fbasics.bypass.booklimiter";
    private final String playerMessage;

    public BookLimiterListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();

        this.plugin = plugin;
        this.pageLimit = config.getInt("book-limiter.page-limit");
        this.playerMessage = language.getString("book-limiter.info.book-too-long");
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        if (event.getPlayer().hasPermission(PERMISSION_BYPASS)) return;

        if (event.getNewBookMeta().getPageCount() > pageLimit) {
            BookMeta newMeta = (BookMeta) plugin.getServer().getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
            // Retain old data
            newMeta.setAuthor(event.getPreviousBookMeta().getAuthor());
            newMeta.setTitle(event.getPreviousBookMeta().getTitle());

            for (int i = 1; i < pageLimit + 1; i++) { // Start at 1 so that we can use this for pages
                newMeta.addPage(event.getPreviousBookMeta().getPage(i));
            }

            event.setNewBookMeta(newMeta);
            event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.playerMessage.replace("{NUMBER}", "" + this.pageLimit)));
        }
    }
}
