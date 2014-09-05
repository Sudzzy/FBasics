package org.originmc.fbasics.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;

public class FBCommand implements CommandExecutor {

	private FBasics plugin;
	public FBCommand(FBasics plugin) {
		this.plugin = plugin;
	}


	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length == 1) {

			args[0] = args[0].toLowerCase();


			if (args[0].matches("reload")) {

				if (!sender.hasPermission(Permissions.fbasicsReload)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				this.plugin.getServer().getPluginManager().disablePlugin(plugin);
				this.plugin.getServer().getPluginManager().enablePlugin(plugin);


				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.reload));
				return true;
			}


			if (args[0].matches("nulled")) {
				return true;
			}


			if (args[0].matches("version|v")) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6FBasics &8// &fUsing version " + plugin.getDescription().getVersion()));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6FBasics &8// &fWritten by &7Sudzzy"));
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&',"&6FBasics &8// &fWebsite: &7http://originmc.org/"));
				return true;
			}
		}


		for (String msg : LanguageSettings.help) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}


		return false;

	}
}