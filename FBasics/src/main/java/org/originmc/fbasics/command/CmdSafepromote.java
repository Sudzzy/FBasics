package org.originmc.fbasics.command;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.settings.SafePromoteSettings;
import org.originmc.fbasics.util.MessageUtils;

import java.util.List;

import static org.bukkit.ChatColor.RED;
import static org.bukkit.ChatColor.YELLOW;

public final class CmdSafepromote extends CommandExecutor {

    private static final String INCORRECT_MESSAGE = RED + "Usage: /fbasics safepromote <user> <old-rank> <new-rank>";

    private static final String INVALID_PLAYER = RED + "Cannot find the player {name}!";

    private static final String VAULT_NOT_ENABLED = RED + "No permissions system to latch onto (Vault)!";

    private static final String CONFIRMATION = YELLOW + "Successfully sent all commands!";

    private final FBasics plugin;

    private final SafePromoteSettings settings;

    public CmdSafepromote(FBasics plugin, CommandSender sender, String[] args, String permission) {
        super(plugin, sender, args, permission);
        this.plugin = plugin;
        settings = plugin.getSettings().getSafePromoteSettings();
    }

    @Override
    public boolean perform() {
        // Do nothing if this module is not enabled.
        if (!settings.isEnabled()) return false;

        // Do nothing if not enough arguments were found.
        if (getArgs().length < 4) {
            getSender().sendMessage(INCORRECT_MESSAGE);
            return true;
        }

        // Do nothing if Vault permissions are not loaded.
        if (plugin.getPermissions() == null || !plugin.getPermissions().hasGroupSupport()) {
            getSender().sendMessage(VAULT_NOT_ENABLED);
            return true;
        }

        // Attempt to find the player.
        Player player = findPlayer();
        if (player == null) {
            getSender().sendMessage(INVALID_PLAYER.replace("{name}", getArgs()[1]));
            return true;
        }

        // Grab the commands depending on whether or not the player is in this old group.
        List<String> commands;
        if (plugin.getPermissions().playerInGroup(player, getArgs()[2])) {
            commands = settings.getSuccessCommands();
        } else {
            commands = settings.getFailedCommands();
        }

        // Execute the related commands.
        for (String command : commands) {
            Bukkit.dispatchCommand(getSender(), command
                    .replace("{name}", player.getName())
                    .replace("{old_group}", getArgs()[2])
                    .replace("{new_group}", getArgs()[3]));
        }
        MessageUtils.sendMessage(getSender(), CONFIRMATION);
        return true;
    }

    private Player findPlayer() {
        if (settings.isAutocomplete()) {
            List<Player> players = Bukkit.matchPlayer(getArgs()[1]);
            if (players.isEmpty()) {
                return null;
            }
            return players.get(0);
        } else {
            return Bukkit.getPlayer(getArgs()[1]);
        }
    }

}
