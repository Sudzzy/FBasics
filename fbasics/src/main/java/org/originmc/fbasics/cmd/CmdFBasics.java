package org.originmc.fbasics.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.originmc.fbasics.FBasics;

import java.util.List;

public class CmdFBasics implements CommandExecutor {

    private static final String PERMISSION_RELOAD = "fbasics.commands.reload";
    private final FBasics plugin;
    private final String messagePermission;
    private final String messageReload;
    private final List<String> messageHelp;

    public CmdFBasics(FBasics plugin) {
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageReload = info + language.getString("general.info.reload");
        this.messageHelp = language.getStringList("general.help");

        plugin.getCommand("fbasics").setExecutor(this);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1) {
            args[0] = args[0].toLowerCase();

            if (args[0].matches("reload")) {

                if (!sender.hasPermission(PERMISSION_RELOAD)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                Bukkit.getPluginManager().disablePlugin(plugin);
                Bukkit.getPluginManager().enablePlugin(plugin);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageReload));
                return true;
            }

            if (args[0].matches("nulled")) {
                return true;
            }

            if (args[0].matches("version|v")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fUsing version " + plugin.getDescription().getVersion()));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fWritten by &7Sudzzy"));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fWebsite: &7http://originmc.org/"));
                return true;
            }
        }

        for (String msg : this.messageHelp) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return false;
    }
}