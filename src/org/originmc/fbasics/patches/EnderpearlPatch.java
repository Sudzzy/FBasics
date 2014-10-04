package org.originmc.fbasics.patches;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.PatchSettings;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;

public class EnderpearlPatch implements Listener {

	private FBasics plugin;
	public EnderpearlPatch(FBasics plugin) {
		this.plugin = plugin;
	}


	Map<String, String> listEnderpearl = new HashMap<String, String>();


	@EventHandler(ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {

		if (!e.getCause().equals(TeleportCause.ENDER_PEARL) || PatchSettings.enderpearlsFactions.contains("{ALL}")) {
			return;
		}


		Player player = e.getPlayer();


		if (player.hasPermission(Permissions.enderpearl)) {
			return;
		}


		Location location = e.getTo();


		if (Bukkit.getPluginManager().getPlugin("Factions") != null && !isInFaction(player, location)) {
			return;
		}


		e.setCancelled(true);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.enderpearlsFactions));
		player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));

	}


	@SuppressWarnings("deprecation")
	@EventHandler
	public void onEnderpearl(PlayerInteractEvent e) {

		Action action = e.getAction();

		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			return;
		}

		Player player = e.getPlayer();
		Material item = player.getItemInHand().getType();

		if (player.hasPermission(Permissions.enderpearl) || item == null || item != Material.ENDER_PEARL) {
			return;
		}

		if (PatchSettings.enderpearlsDisable) {
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.enderpearlsDisabled));
			e.setCancelled(true);
			player.updateInventory();
			return;
		}


        Location location = player.getLocation();
        Material block1 = location.getBlock().getType();
        Material block2 = location.getBlock().getRelative(0, 1, 0).getType();


        if (PatchSettings.enderpearlsBlocks && (!block1.equals(Material.AIR) || !block2.equals(Material.AIR))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.enderpearlsBlock));
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

		if (this.listEnderpearl.containsKey(player.getName())) {
			String[] cooldownInfo = this.listEnderpearl.get(player.getName()).split("-");
			long remaining = Integer.parseInt(cooldownInfo[1]) - (System.currentTimeMillis() - Long.parseLong(cooldownInfo[0])) / 1000L;

			String enderpearlMessage = LanguageSettings.enderpearlsCooldown.replace("{REMAINING}", "" + remaining);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', enderpearlMessage));

			e.setCancelled(true);
			player.updateInventory();
			return;
		}

		setCooldown(player.getName(), PatchSettings.enderpearlsCooldown);
	}


	@EventHandler
	public void onDoorInteract(PlayerInteractEvent e) {

		Action action = e.getAction();

		if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
			return;
		}

		Player player = e.getPlayer();

		if (player.hasPermission(Permissions.enderpearl)) {
			return;
		}

		Material block = e.getClickedBlock().getType();

		if (PatchSettings.enderpearlsDoors.contains(block) && !this.listEnderpearl.containsKey(player.getName())) {
			setCooldown(player.getName(), PatchSettings.enderpearlsDoorCooldown);
		}
	}


	private void setCooldown(final String player, int cooldown) {

		listEnderpearl.put(player, System.currentTimeMillis() + "-" + cooldown);

		new BukkitRunnable() {
			public void run() {
				listEnderpearl.remove(player);
			}
		}.runTaskLaterAsynchronously(this.plugin, cooldown * 20);
	}


	private boolean isInFaction(Player player, Location location) {

		String version = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();


		if (version.startsWith("1")) {

			FLocation flocation = new FLocation(location);
			com.massivecraft.factions.Faction faction1 = Board.getFactionAt(flocation);
			com.massivecraft.factions.Faction faction2 = FPlayers.i.get(player).getFaction();


			for (String faction : PatchSettings.enderpearlsFactions) {

				if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
					return false;
				}


				if (faction.equalsIgnoreCase(faction1.getTag()) || faction.equalsIgnoreCase(faction1.getTag().substring(2))) {
					return false;
				}
			}
		}


		else if (version.startsWith("2")) {

			com.massivecraft.factions.entity.Faction faction1 = BoardColls.get().getFactionAt(PS.valueOf(location));
			com.massivecraft.factions.entity.Faction faction2 = UPlayer.get(player).getFaction();


			for (String faction : PatchSettings.enderpearlsFactions) {

				if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
					return false;
				}


				if (faction.equalsIgnoreCase(faction1.getName()) || faction.equalsIgnoreCase(faction1.getName().substring(2))) {
					return false;
				}
			}
		}

		return true;
	}
}