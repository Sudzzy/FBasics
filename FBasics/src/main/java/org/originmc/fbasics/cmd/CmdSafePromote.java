package org.originmc.fbasics.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;

import java.util.List;

public class CmdSafePromote implements CommandExecutor {

    private final boolean autoComplete;

    private final FBasics plugin;

    private final String messageInvalidPlayer;

    private final String messageVault;

    private final List<String> commandsFailed;

    private final List<String> commandsSuccess;

    public CmdSafePromote(FBasics plugin) {
        // Load all the settings for the SafePromote command
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.plugin = plugin;
        this.autoComplete = config.getBoolean("safe-promote.auto-complete-names");
        this.commandsFailed = config.getStringList("safe-promote.failed-commands");
        this.commandsSuccess = config.getStringList("safe-promote.success-commands");
        this.messageInvalidPlayer = error + language.getString("general.error.player");
        this.messageVault = error + language.getString("general.error.vault");

        // If config states SafePromote should be enabled, register its command
        if (config.getBoolean("safe-promote.enabled")) {
            plugin.getCommand("safepromote").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Do nothing if incorrect amount of arguments
        if (args.length != 3) {
            return false;
        }

        // If stated in config, autocomplete the username
        String name = args[0];
        if (autoComplete) {
            name = autoComplete(name);
        }

        // Do nothing if Vault permissions is not loaded
        if (plugin.getPermission() == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageVault));
            return true;
        }

        // Do nothing if the player is not online
        @SuppressWarnings("deprecation")
        Player player = Bukkit.getPlayer(name);
        if (player == null) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageInvalidPlayer));
            return true;
        }

        // Check if the player is within the specified group
        String oldRank = args[1];
        String newRank = args[2];
        if (plugin.getPermission().playerInGroup(player, oldRank)) {
            // Execute success commands specified in config
            for (String command : commandsSuccess) {
                Bukkit.dispatchCommand(sender, command
                        .replace("{NAME}", name)
                        .replace("{OLD_GROUP}", oldRank)
                        .replace("{NEW_GROUP}", newRank));
            }

            return true;
        } else {
            // Execute failed commands specified in config
            for (String command : commandsFailed) {
                Bukkit.dispatchCommand(sender, command
                        .replace("{NAME}", name)
                        .replace("{OLD_GROUP}", oldRank)
                        .replace("{NEW_GROUP}", newRank));
            }

            return true;
        }
    }

    private String autoComplete(String name) {
        // Attempt to find a player matching the specified username
        name = name.toLowerCase();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(name)) {
                return player.getName();
            }
        }

        return name;
    }

}