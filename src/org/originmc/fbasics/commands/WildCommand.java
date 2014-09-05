package org.originmc.fbasics.commands;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.WildSettings;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.FactionColls;
import com.massivecraft.massivecore.ps.PS;

public class WildCommand implements CommandExecutor {

	@SuppressWarnings("unused")
	private FBasics plugin;
	public WildCommand(FBasics plugin) {
		this.plugin = plugin;
	}


	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.console));
			return true;
		}


		if (!sender.hasPermission(Permissions.wilderness)) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
		}


		Player player = (Player) sender;
		World world = player.getWorld();


		if (!WildSettings.worlds.contains(world.getName())) {
			sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.wildernessWorld));
			return true;
		}


		for (int counter = 0; counter < 25; counter++) {

			Random r = new Random();
			int xRange = WildSettings.minRange + r.nextInt(WildSettings.maxRange - WildSettings.minRange);
			int zRange = WildSettings.minRange + r.nextInt(WildSettings.maxRange - WildSettings.minRange);
			int x = WildSettings.xCenter + xRange;
			int z = WildSettings.zCenter + zRange;

			if (teleportCheck(player, world, x, z)) {
				return true;
			}
		}


		sender.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.wildernessFailed));
		return true;

	}


	private boolean teleportCheck(Player sender, World world, int z, int x) {

		int y = world.getHighestBlockYAt(x, z);
		Block highest = world.getBlockAt(x, y - 1, z);
		String blockname = highest.getType().toString();


		if (WildSettings.blocks.contains(blockname)) {
			return false;
		}


		if (Bukkit.getPluginManager().getPlugin("Factions") != null) {

			String version = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();

			if (version.startsWith("1")) {
				FLocation flocation = new FLocation(highest.getLocation());
				if (!Board.getFactionAt(flocation).isNone()) {
					return false;
				}
			}

			if (version.startsWith("2")) {
				com.massivecraft.factions.entity.Faction wilderness = FactionColls.get().getForWorld(world.getName()).getNone();
				com.massivecraft.factions.entity.Faction faction = BoardColls.get().getFactionAt(PS.valueOf(highest));

				if (faction != wilderness) {
					return false;
				}
			}
		}


		teleportPlayer(sender, world, x, z);
		return true;

	}

	private boolean teleportPlayer(Player player, World world, int x, int z) {

		if (player == null) {
			return true;
		}


		int y = world.getHighestBlockYAt(x, z);


		player.teleport(new Location(world, x + 0.5D, y, z + 0.5D));
		String msg = LanguageSettings.wildernessSuccess.replace("{name}", player.getName()).replace("{X}", "" + x).replace("{Y}", "" + y).replace("{Z}", "" + z).replace("{WORLD}", world.getName());
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
		return true;
	}
}