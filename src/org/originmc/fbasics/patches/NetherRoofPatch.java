package org.originmc.fbasics.patches;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;
import org.originmc.fbasics.settings.LanguageSettings;

public class NetherRoofPatch implements Listener {

	@SuppressWarnings("unused")
	private FBasics plugin;
	public NetherRoofPatch(FBasics plugin) {
		this.plugin = plugin;
	}


	@EventHandler(ignoreCancelled = true)
	public void onTeleport(PlayerTeleportEvent e) {

		Player player = e.getPlayer();


		if (player.hasPermission(Permissions.nether)) {
			return;
		}


		World world = e.getTo().getWorld();
		Location location = e.getTo();


		if (world.getEnvironment().equals(World.Environment.NETHER) && location.getY() >= 126.0D) {
			e.setCancelled(true);
			player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.netherCancelled));
			return;
		}
	}
}