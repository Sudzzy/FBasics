package org.originmc.fbasics.patches;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.PatchSettings;

public class AntiLooterPatch implements Listener {

	private FBasics plugin;
	public AntiLooterPatch(FBasics plugin) {
		this.plugin = plugin;
	}


	private List<UUID> antiSpam = new ArrayList<UUID>();


	@EventHandler(ignoreCancelled = true)
	public void onPickup(PlayerPickupItemEvent e) {

		Player player = e.getPlayer();
		UUID uuid = player.getUniqueId();
		Item item = e.getItem();
		Boolean meta = item.hasMetadata("fbasics-antiloot");


		if (player.hasPermission(Permissions.antiLooter) || !meta) {
			return;
		}


		String getValues = e.getItem().getMetadata("fbasics-antiloot").get(0).asString();
		String[] data = getValues.split("-");


		if (player.getName().equals(data[0])) {
			e.getItem().removeMetadata("fbasics-antiloot", plugin);
			return;
		}


		long remaining = System.currentTimeMillis() - Long.valueOf(data[1]);


		if (remaining >= PatchSettings.antiLooterTime * 1000) {
			e.getItem().removeMetadata("fbasics-antiloot", plugin);
			return;
		}


		e.setCancelled(true);


		if (antiSpam.contains(uuid)) {
			return;
		}


		setAntiSpamCooldown(uuid);
		long cooldown = PatchSettings.antiLooterTime - remaining / 1000L;
		String lootMessage = LanguageSettings.antiLooterProtected.replace("{REMAINING}", "" + cooldown);
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', lootMessage));
	}


	@EventHandler
	public void onDeath(PlayerDeathEvent e) {

		if (e.getEntity().getKiller() == null) {
			return;
		}


		final Player killer = e.getEntity().getPlayer().getKiller();
		Player victim = e.getEntity().getPlayer();


		String dropped = LanguageSettings.antiLooterDropped.replace("{TIME}", "" + PatchSettings.antiLooterTime);
		killer.sendMessage(ChatColor.translateAlternateColorCodes('&', dropped));


		new BukkitRunnable() {
			public void run() {
				killer.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.antiLooterTimerFinish));
			}
		}.runTaskLaterAsynchronously(plugin, PatchSettings.antiLooterTime * 20);


		for (ItemStack a : e.getDrops()) {
			Entity item = victim.getWorld().dropItemNaturally(victim.getLocation(), a);
			item.setMetadata("fbasics-antiloot", new FixedMetadataValue(plugin, killer.getName() + "-" + System.currentTimeMillis()));
		}


		e.getDrops().clear();

	}


	public void setAntiSpamCooldown(final UUID uuid) {

		antiSpam.add(uuid);

		new BukkitRunnable() {
			@Override
			public void run() {
				antiSpam.remove(uuid);
				cancel();
			}
		}.runTaskLaterAsynchronously(plugin, 20L);
	}
}