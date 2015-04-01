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

    private static final String PERMISSION_RELOAD = "fbasics.command.reload";

    private final FBasics plugin;

    private final String messagePermission;

    private final String messageReload;

    private final List<String> messageHelp;

    public CmdFBasics(FBasics plugin) {
        // Load all the settings for the FBasics command
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageReload = info + language.getString("general.info.reload");
        this.messageHelp = language.getStringList("general.help");

        // Register the FBasics command
        plugin.getCommand("fbasics").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Send help page if there are no arguments
        if (args.length != 1) {
            for (String msg : messageHelp) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }

            return true;
        }

        // Check if user intends to reload the plugin
        args[0] = args[0].toLowerCase();
        if (args[0].matches("reload")) {
            // Do nothing if sender does not have permission
            if (!sender.hasPermission(PERMISSION_RELOAD)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                return true;
            }

            // Reload the plugin
            // TODO: Only needs to reload configuration...
            Bukkit.getPluginManager().disablePlugin(plugin);
            Bukkit.getPluginManager().enablePlugin(plugin);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageReload));
            return true;
        }

        // Do nothing if command is "/fbasics null", sometimes useful when overriding factions commands
        if (args[0].matches("null")) {
            return true;
        }

        // Send the sender information about the plugin
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fUsing version " + plugin.getDescription().getVersion()));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fWritten by &7Sudzzy"));
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6FBasics &8// &fWebsite: &7http://originmc.org/"));
        return true;
    }

}