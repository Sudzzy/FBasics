package org.originmc.fbasics.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class MessageUtils {

    /**
     * Sends the player a message with chat colors converted if the message is not empty.
     *
     * @param sender  the player to send the message to.
     * @param message the message to send.
     */
    public static void sendMessage(CommandSender sender, String message) {
        if (!message.isEmpty()) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

}
