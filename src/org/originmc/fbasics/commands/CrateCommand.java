package org.originmc.fbasics.commands;

import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.database.DatabaseManager;
import org.originmc.fbasics.settings.CrateSettings;
import org.originmc.fbasics.settings.LanguageSettings;

public class CrateCommand implements CommandExecutor {

	private FBasics plugin;
	public CrateCommand(FBasics plugin) {
		this.plugin = plugin;
	}


	DatabaseManager database = new DatabaseManager(this.plugin);


	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length == 1) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.console));
				return true;
			}


			Player player = (Player) sender;
			args[0] = args[0].toLowerCase();


			/*
			 * Balance (Self)
			 */
			if (args[0].matches("bal|balance")) {

				if (!player.hasPermission(Permissions.crateBalance)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = player.getName().toLowerCase();
				int crates = this.database.getCrates(name);

				String msg = LanguageSettings.cratesBalance.replace("{CRATES}", "" + crates);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

				return true;

			}


			/*
			 * Open
			 */
			if (args[0].matches("open")) {

				if (!player.hasPermission(Permissions.crateOpen)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = player.getName().toLowerCase();
				int crates = this.database.getCrates(name);


				if (crates <= 0) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cratesNotEnough));
					return true;
				}


				if (CrateSettings.algorithm) {
					newAlgorithm(player);
				} else {
					oldAlgorithm(player);
				}


				int removed = crates - 1;
				this.database.setCrates(name, removed);
				return true;
			}
		}


		if (args.length == 2) {

			/*
			 * Balance (Other)
			 */
			if ((args[0].toLowerCase().matches("bal|balance"))) {

				if (!sender.hasPermission(Permissions.crateBalanceOther)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = args[1];
				int crates = this.database.getCrates(name);


				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cratesBalanceOther.replace("{NAME}", name).replace("{CRATES}", "" + crates)));
				return true;
			}
		}


		if (args.length == 3) {

			args[0] = args[0].toLowerCase();


			/*
			 * Set
			 */
			if (args[0].matches("set")) {

				if (!sender.hasPermission(Permissions.crateChange)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = args[1].toLowerCase();
				int crates = getValidInteger(args[2]);


				this.database.setCrates(name, crates);


				String msg = LanguageSettings.cratesChanged.replace("{NAME}", name).replace("{CRATES}", "" + crates);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				return true;
			}


			/*
			 * Add
			 */
			if (args[0].matches("add|give")) {

				if (!sender.hasPermission(Permissions.crateChange)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = args[1].toLowerCase();
				int crates = getValidInteger(args[2]);
				int added = this.database.getCrates(name) + crates;


				this.database.setCrates(name, added);


				String msg = LanguageSettings.cratesChanged.replace("{NAME}", name).replace("{CRATES}", "" + added);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				return true;
			}


			/*
			 * Remove
			 */
			if (args[0].matches("remove|rem")) {

				if (!sender.hasPermission(Permissions.crateChange)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				String name = args[1].toLowerCase();
				int crates = getValidInteger(args[2]);
				int removed = this.database.getCrates(name) - crates;


				this.database.setCrates(name, removed);


				String msg = LanguageSettings.cratesChanged.replace("{NAME}", name).replace("{CRATES}", "" + removed);
				sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
				return true;
			}


			/*
			 * Pay
			 */
			if (args[0].matches("pay")) {

				if (!sender.hasPermission(Permissions.cratePay)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
					return true;
				}


				if (!(sender instanceof Player)) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.console));
					return true;
				}


				@SuppressWarnings("deprecation")
				Player receiver = Bukkit.getPlayer(args[1]);
				Player player = (Player) sender;

				String playerName = player.getName().toLowerCase();
				String receiverName = args[1].toLowerCase();

				int crates = getValidInteger(args[2]);
				int playerCrates = this.database.getCrates(playerName);


				if (receiver == null) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.invalidPlayer));
					return true;
				}


				if (crates <= 0) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cratesInvalid));
					return true;
				}


				if (playerCrates < crates) {
					sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cratesNotEnough));
					return true;
				}


				playerCrates = playerCrates - crates;
				this.database.setCrates(playerName, playerCrates);

				int receiverCrates = crates + this.database.getCrates(receiverName);
				this.database.setCrates(receiverName, receiverCrates);

				String playerMsg = LanguageSettings.cratesPaymentSent.replace("{CRATES}", String.valueOf(crates)).replace("{NAME}", receiver.getName());
				String receiverMsg = LanguageSettings.cratesPaymentReceived.replace("{CRATES}", String.valueOf(crates)).replace("{NAME}", player.getName());

				player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMsg));
				receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', receiverMsg));

				return true;
			}
		}


		for (String msg : LanguageSettings.help) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		}


		return false;

	}


	private void newAlgorithm(Player player) {

		String name = player.getName().toLowerCase();
		Random random = new Random();


		for (String reward : CrateSettings.rewards) {

			double chance = 1.0D / Double.parseDouble(reward);


			if (chance < random.nextDouble()) {
				continue;
			}


			String message = CrateSettings.rewardMessages.get(reward);
			List<String> commands = CrateSettings.rewardCommands.get(reward);


			for (String cmd : commands) {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
			}


			player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
		}
	}


	private void oldAlgorithm(Player player) {

		String name = player.getName().toLowerCase();

		Random random = new Random();

		int rewards = CrateSettings.rewards.size();
		int reward = random.nextInt(rewards);

		String path = String.valueOf(CrateSettings.rewards.toArray()[reward]);
		String message = CrateSettings.rewardMessages.get(path);

		List<String> commands = CrateSettings.rewardCommands.get(path);


		for (String cmd : commands) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
		}


		player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
	}


	private int getValidInteger(String integer) {

		try {
			return Integer.parseInt(integer);
		} catch (NumberFormatException e) {
			return 0;
		}

	}

}