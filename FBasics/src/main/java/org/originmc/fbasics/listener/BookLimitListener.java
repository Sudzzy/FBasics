package org.originmc.fbasics.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.settings.AntiGlitchSettings;

import static org.originmc.fbasics.util.MessageUtils.sendMessage;

public final class BookLimitListener implements Listener {

    private final AntiGlitchSettings settings;

    public BookLimitListener(FBasics plugin) {
        settings = plugin.getSettings().getAntiGlitchSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void limitPageCount(PlayerEditBookEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.AntiGlitch.BOOK_LIMIT)) return;

        // Do nothing if the page count is smaller or equal to the limit.
        if (event.getNewBookMeta().getPageCount() <= settings.getBookLimit()) return;

        // Retain old book data.
        BookMeta newMeta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
        newMeta.setAuthor(event.getPreviousBookMeta().getAuthor());
        newMeta.setTitle(event.getPreviousBookMeta().getTitle());

        // Remove any pages in excess of the page limit.
        newMeta.setPages(newMeta.getPages().subList(0, settings.getBookLimit() - 1));
        event.setNewBookMeta(newMeta);

        // Send player a message about the page limitation.
        sendMessage(player, settings.getBookLimitMessage().replace("{limit}", "" + settings.getBookLimit()));
    }

}
