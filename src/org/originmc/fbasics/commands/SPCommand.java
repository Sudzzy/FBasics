package org.originmc.fbasics.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.SPSettings;

public class SPCommand implements CommandExecutor {

	private FBasics plugin;
	public SPCommand(FBasics plugin) {
		this.plugin = plugin;
	}


	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length == 3) {

			if (!sender.hasPermission(Permissions.safepromote)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
				return true;
			}


			String name = args[0];
			String oldRank = args[1];
			String newRank = args[2];


			if (SPSettings.autoComplete) {
				name = autoComplete(name);
			}


			@SuppressWarnings("deprecation")
			Player player = Bukkit.getPlayer(name);


			if (player == null) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.invalidPlayer));
				return true;
			}


			if (this.plugin.permission.playerInGroup(player, oldRank)) {

				for (String commands : SPSettings.success) {
					String successCommand = commands.replace("{NAME}", name).replace("{OLD_GROUP}", oldRank).replace("{NEW_GROUP}", newRank);
					Bukkit.dispatchCommand(sender, ChatColor.translateAlternateColorCodes('&', successCommand));
				}

				return true;

			} else {

				for (String command : SPSettings.failed) {
					String failedCommand = command.replace("{NAME}", name).replace("{OLD_GROUP}", oldRank).replace("{NEW_GROUP}", newRank);
					Bukkit.dispatchCommand(sender, ChatColor.translateAlternateColorCodes('&', failedCommand));
				}

				return true;
			}
		}


		for (String msg : LanguageSettings.help) {
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