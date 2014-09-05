package org.originmc.fbasics.patches;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.PatchSettings;

public class CactusPatch implements Listener {

	public CactusPatch() { }


	@EventHandler
	public void onPhysics(BlockPhysicsEvent e) {

		Block block = e.getBlock();

		if (!block.getType().equals(Material.CACTUS)) {
			return;
		}

		if (checkChest(block.getRelative(BlockFace.UP))
				|| checkChest(block.getRelative(BlockFace.DOWN))
				|| checkChest(block.getRelative(BlockFace.NORTH))
				|| checkChest(block.getRelative(BlockFace.SOUTH))
				|| checkChest(block.getRelative(BlockFace.EAST))
				|| checkChest(block.getRelative(BlockFace.WEST))) {
			e.setCancelled(true);
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlace(BlockPlaceEvent e) {

		Player player = e.getPlayer();
		Block block = e.getBlockPlaced();

		if (!PatchSettings.cactusBlocks.contains(block.getType())) {
			return;
		}

		if (checkBlock(block.getRelative(BlockFace.UP), player)
				|| checkBlock(block.getRelative(BlockFace.DOWN), player)
				|| checkBlock(block.getRelative(BlockFace.NORTH), player)
				|| checkBlock(block.getRelative(BlockFace.SOUTH), player)
				|| checkBlock(block.getRelative(BlockFace.EAST), player)
				|| checkBlock(block.getRelative(BlockFace.WEST), player)) {
			e.setCancelled(true);
		}
	}


	private boolean checkBlock(Block block, Player player) {

		Material material = block.getType();


		if (!material.equals(Material.CACTUS)) {
			return false;
		}


		player.sendMessage(ChatColor.translateAlternateColorCodes('&', LanguageSettings.cactusBlock));
		return true;
	}


	private boolean checkChest(Block block) {
		Material material = block.getType();
		return PatchSettings.cactusBlocks.contains(material);
	}
}