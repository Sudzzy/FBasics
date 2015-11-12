package org.originmc.fbasics.command;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;

@Data
public abstract class CommandExecutor {

    private final FBasics plugin;

    private final CommandSender sender;

    private final String[] args;

    private final String permission;

    private boolean requiresPlayer;

    public CommandExecutor(FBasics plugin, CommandSender sender, String[] args, String permission) {
        this.plugin = plugin;
        this.sender = sender;
        this.args = args;
        this.permission = permission;
    }

    /**
     * Attempts to check if the sender has permission and if the sender has a
     * user profile attached from this plugin if required.
     *
     * @return true if valid command, otherwise false.
     */
    public boolean execute() {
        // Do nothing if the sender does not have permission.
        if (!(sender instanceof ConsoleCommandSender) && !sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        // Do nothing if sender is not a player.
        if (requiresPlayer && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can execute this command!");
            return true;
        }

        // Perform the command.
        return perform();
    }

    /**
     * Executes the command.
     *
     * @return true if valid command, otherwise false.
     */
    public abstract boolean perform();

}
