package org.originmc.fbasics.patches;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.CommandSettings;
import org.originmc.fbasics.settings.LanguageSettings;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;

public class CommandPatch implements Listener {

	private FBasics plugin;
	public CommandPatch(FBasics plugin) {
		this.plugin = plugin;
	}


	private List<UUID> commandQueue = new ArrayList<UUID>();
	private Map<UUID, Map<String, Long>> activeCooldowns = new HashMap<UUID, Map<String, Long>>();


	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {

		final Player player = e.getPlayer();
		final UUID uuid = player.getUniqueId();


		if (!commandQueue.contains(uuid)) {
			return;
		}


		final Location location = player.getLocation();
		final String world = location.getWorld().getName();

		new BukkitRunnable() {
			public void run() {

				String newWorld = player.getWorld().getName();

				if (player == null || !commandQueue.contains(uuid) || !world.equalsIgnoreCase(newWorld)) {
					return;
				}

				double distance = location.distance(player.getLocation());

				if (distance > 1.0D) {
					commandQueue.remove(uuid);
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsCancelled));
				}
			}
		}.runTaskLaterAsynchronously(this.plugin, 10L);
	}


	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onDamage(EntityDamageEvent e) {

		if (!(e.getEntity() instanceof Player)) {
			return;
		}


		Player player = (Player) e.getEntity();
		UUID uuid = player.getUniqueId();


		if (commandQueue.remove(uuid)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsCancelled));
		}
	}


	@EventHandler(ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent event) {

		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();


		if (commandQueue.remove(uuid)) {
			player.sendMessage(LanguageSettings.commandsCancelled);
		}
	}


	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommandLowest(PlayerCommandPreprocessEvent e) {

		String message = e.getMessage();

		if (!message.matches(CommandSettings.priority)) {
			return;
		}

		Player player = e.getPlayer();
		List<String> cmdSettings = parseCommand(player, message);


		e.setMessage(cmdSettings.get(0));
		e.setCancelled(Boolean.valueOf(cmdSettings.get(1)));

	}


	@EventHandler(ignoreCancelled=true, priority = EventPriority.HIGHEST)
	public void onCommandHighest(PlayerCommandPreprocessEvent e) {

		String message = e.getMessage();

		if (message.matches(CommandSettings.priority)) {
			return;
		}

		Player player = e.getPlayer();
		List<String> cmdSettings = parseCommand(player, message);


		e.setMessage(cmdSettings.get(0));
		e.setCancelled(Boolean.valueOf(cmdSettings.get(1)));

	}


	private List<String> parseCommand(Player player, String message) {

		String editor = null;
		String matcher = null;
		String testMessage = message;
		boolean cancelled = false;


		if (CommandSettings.ignoreCase) {
			testMessage = message.toLowerCase();
		}


		for (String m : CommandSettings.matcher.keySet())
            if (testMessage.matches(m)) {
                editor = CommandSettings.matcher.get(m);
                matcher = m;
            }


		if (editor == null) {
			return Arrays.asList(message, "false");
		}


		int cooldown = CommandSettings.cooldown.get(editor);
		int warmup = CommandSettings.warmup.get(editor);
		double price = CommandSettings.price.get(editor);
		String alias = CommandSettings.alias.get(editor);
		String permission = CommandSettings.permission.get(editor);
		List<String> blocks = CommandSettings.blocks.get(editor);
		List<String> factions = CommandSettings.factions.get(editor);


		/**
		 * Alias
		 */
		if (alias != null) {

			String[] args = message.split(" ");
			testMessage = message.replaceAll(matcher.replace(".*", ""), "");
			message = alias;
			message = message.replace("{ALL_ARGS}", testMessage);
			args[0] = args[0].substring(1);

			for (int c = 0; c < args.length; c++) {
				message = message.replace("{ARG:" + c + "}", args[c]);
			}
		}


		/**
		 * Permission
		 */
		if (permission != null && !player.hasPermission(permission)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.permission));
			message = "/fbasics nulled";
			return Arrays.asList(message, "true");
		}


		/**
		 * Blocks
		 */
		if (!blocks.isEmpty() && !player.hasPermission(Permissions.commandsBlocks)) {

			Location location = player.getLocation();
			String block1 = location.getBlock().getType().toString();
			String block2 = location.getBlock().getRelative(0, 1, 0).getType().toString();


			if (!blocks.contains(block1) || !blocks.contains(block2)) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsBlock));
				message = "/fbasics nulled";
				return Arrays.asList(message, "true");
			}
		}


		/**
		 * Factions
		 */
		if (!factions.isEmpty() && Bukkit.getPluginManager().getPlugin("Factions") != null && !player.hasPermission(Permissions.commandsTerritory) && isInFactionLand(player, factions)) {
			message = "/fbasics nulled";
			return Arrays.asList(message, "true");
		}


		/**
		 * Cooldown
		 */
		if (cooldown > 0 && !player.hasPermission(Permissions.commandsCooldown) && setCooldown(player, cooldown, editor)) {
			message = "/fbasics nulled";
			return Arrays.asList(message, "true");
		}


		/**
		 * Economy
		 */
		if (this.plugin.economy != null && !player.hasPermission(Permissions.commandsEconomy) && price != 0) {

			double balance = this.plugin.economy.getBalance(player);

			if (balance < price) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsInsufficientFunds));
				message = "/fbasics nulled";
				return Arrays.asList(message, "true");
			}

			this.plugin.economy.withdrawPlayer(player, price);
			String msg = LanguageSettings.commandsPaid.replace("{MONEY}", String.valueOf(price));
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));

		}


		/**
		 * Warmup
		 */
		if (warmup > 0 && !player.hasPermission(Permissions.commandsWarmup)) {
			setWarmup(player, warmup, message);
			cancelled = true;
			message = "/fbasics nulled";
		}


		return Arrays.asList(message, String.valueOf(cancelled));
	}


	private boolean setCooldown(Player player, int cooldown, String editor) {

		UUID uuid = player.getUniqueId();
		Map<String, Long> playerCooldowns = new HashMap<String, Long>();


		if (this.activeCooldowns.containsKey(uuid)) {

			playerCooldowns = activeCooldowns.get(uuid);

			if (this.activeCooldowns.get(uuid).containsKey(editor)) {

				long activeCooldown = activeCooldowns.get(uuid).get(editor);
				long remaining = cooldown - (System.currentTimeMillis() - activeCooldown) / 1000L;

				if (remaining > 0L) {
					String msg = LanguageSettings.commandsCooldown.replace("{COOLDOWN}", String.valueOf(remaining));
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
					return true;
				}
			}
		}


		playerCooldowns.put(editor, System.currentTimeMillis());
		activeCooldowns.put(player.getUniqueId(), playerCooldowns);
		return false;

	}


	private void setWarmup(final Player player, int warmup, final String command) {

		final UUID uuid = player.getUniqueId();


		if (commandQueue.contains(uuid)) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsWarmupDouble));
			return;
		}


		String warmupMessage = LanguageSettings.commandsWarmup.replace("{WARMUP}", "" + warmup);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', warmupMessage));
		commandQueue.add(uuid);


		new BukkitRunnable() {
			@Override
			public void run() {
				if (commandQueue.contains(uuid) && player != null) {
					commandQueue.remove(uuid);
					Bukkit.dispatchCommand(player, command.substring(1));
				}
			}
		}.runTaskLater(this.plugin, warmup * 20);
	}


	private boolean isInFactionLand(Player player, List<String> factions) {

		Location location = player.getLocation();
		String factionsVersion = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();


		if (factionsVersion.startsWith("1")) {

			FLocation flocation = new FLocation(location);
			com.massivecraft.factions.Faction faction1 = Board .getFactionAt(flocation);
			com.massivecraft.factions.Faction faction2 = FPlayers.i.get(player).getFaction();

			for (String f : factions) {
				if ((f.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) || f.equalsIgnoreCase(faction1.getTag()) || f.equalsIgnoreCase(faction1.getTag().substring(2))) {
					return false;
				}
			}
		}

		else if (factionsVersion.startsWith("2")) {

			com.massivecraft.factions.entity.Faction faction1 = BoardColls.get().getFactionAt(PS.valueOf(location));
			com.massivecraft.factions.entity.Faction faction2 = UPlayer.get(player).getFaction();

			for (String f : factions) {
				if ((f.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) || f.equalsIgnoreCase(faction1.getName()) || f.equalsIgnoreCase(faction1.getName().substring(2))) {
					return false;
				}
			}
		}

		player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.commandsFaction));
		return true;
	}
}