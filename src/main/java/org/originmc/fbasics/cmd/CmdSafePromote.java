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
    private final String messagePermission;
    private final List<String> commandsFailed;
    private final List<String> commandsSuccess;
    private final List<String> messageHelp;
    private final String permissionPromote = "fbasics.commands.safepromote";

    public CmdSafePromote(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.plugin = plugin;
        this.autoComplete = config.getBoolean("safe-promote.auto-complete-names");
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageHelp = language.getStringList("general.help");
        this.commandsFailed = config.getStringList("safe-promote.failed-commands");
        this.commandsSuccess = config.getStringList("safe-promote.success-commands");
        this.messageInvalidPlayer = error + language.getString("general.error.player");
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 3) {
            if (!sender.hasPermission(this.permissionPromote)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                return true;
            }

            String name = args[0];
            String oldRank = args[1];
            String newRank = args[2];

            if (this.autoComplete) {
                name = autoComplete(name);
            }

            @SuppressWarnings("deprecation")
            Player player = Bukkit.getPlayer(name);

            if (player == null) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageInvalidPlayer));
                return true;
            }

            if (this.plugin.getPermission().playerInGroup(player, oldRank)) {
                for (String commands : this.commandsSuccess) {
                    String successCommand = commands.replace("{NAME}", name).replace("{OLD_GROUP}", oldRank).replace("{NEW_GROUP}", newRank);
                    Bukkit.dispatchCommand(sender, ChatColor.translateAlternateColorCodes('&', successCommand));
                }

                return true;

            } else {
                for (String command : this.commandsFailed) {
                    String failedCommand = command.replace("{NAME}", name).replace("{OLD_GROUP}", oldRank).replace("{NEW_GROUP}", newRank);
                    Bukkit.dispatchCommand(sender, ChatColor.translateAlternateColorCodes('&', failedCommand));
                }

                return true;
            }
        }

        for (String msg : this.messageHelp) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return false;
    }

    private String autoComplete(String name) {
        name = name.toLowerCase();

        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().startsWith(name)) {
                name = player.getName();
                return name;
            }
        }

        return name;
    }
}