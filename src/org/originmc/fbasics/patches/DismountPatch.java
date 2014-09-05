package org.originmc.fbasics.patches;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Permissions;

public class DismountPatch implements Listener {

	private FBasics plugin;
	public DismountPatch(FBasics plugin) {
		this.plugin = plugin;
	}


	@EventHandler(ignoreCancelled = true)
	public void onExit(VehicleExitEvent event) {

		final Entity entity = event.getExited();


		if (!(entity instanceof Player)) {
			return;
		}


		final Location location = event.getVehicle().getLocation();


		if (((Player) entity).hasPermission(Permissions.dismount)) {
			return;
		}


		new BukkitRunnable() {
			@Override
			public void run() {
				entity.teleport(location);
			}
		}.runTask(plugin);
	}
}