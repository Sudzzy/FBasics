package org.originmc.fbasics.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;

public class PatchSettings {

	/*
	 * Anti Looter
	 */
	public static boolean antiLooterEnabled;
	public static int antiLooterTime;


	/*
	 * Boat Glitch
	 */
	public static boolean boatEnabled;


	/*
	 * Cactus Glitch
	 */
	public static boolean cactusEnabled;
	public static List<Material> cactusBlocks;


	/*
	 * Dismount Glitch
	 */
	public static boolean dismountEnabled;


	/*
	 * Enderpearl Glitch
	 */
	public static boolean enderpearlsEnabled;
	public static boolean enderpearlsDisable;
	public static int enderpearlsCooldown;
	public static int enderpearlsDoorCooldown;
	public static List<String> enderpearlsFactions;
	public static List<Material> enderpearlsDoors = Arrays.asList(new Material[] { Material.WOOD_DOOR, Material.TRAP_DOOR,Material.WOODEN_DOOR });


	/*
	 * Nether Glitch
	 */
	public static boolean netherEnabled;


	public static void loadPatchSettings() {

		antiLooterEnabled = SettingsManager.getConfig().getBoolean("anti-looter.enabled");


		if (antiLooterEnabled) {
			antiLooterTime = SettingsManager.getConfig().getInt("anti-looter.protection-duration");
		}


		dismountEnabled = SettingsManager.getConfig().getBoolean("patcher.dismount-glitch");


		boatEnabled = SettingsManager.getConfig().getBoolean("patcher.boat-glitch");


		netherEnabled = SettingsManager.getConfig().getBoolean("patcher.nether-glitch");


		cactusEnabled = SettingsManager.getConfig().getBoolean("patcher.cactus-dupe.enabled");


		if (cactusEnabled) {

			List<Material> tempCactusBlocks = new ArrayList<Material>();
			for (String block : SettingsManager.getConfig().getStringList("patcher.cactus-dupe.block-placement-near-cactus")) {
				tempCactusBlocks.add(Material.getMaterial(block));
			}

			cactusBlocks = tempCactusBlocks;
		}


		enderpearlsEnabled = SettingsManager.getConfig().getBoolean("patcher.enderpearls.enabled");


		if (enderpearlsEnabled) {

			enderpearlsDisable = SettingsManager.getConfig().getBoolean("patcher.enderpearls.disable-all-enderpearls");
			enderpearlsCooldown = SettingsManager.getConfig().getInt("patcher.enderpearls.cooldown");
			enderpearlsDoorCooldown = SettingsManager.getConfig().getInt("patcher.enderpearls.door-cooldown");
			enderpearlsFactions = SettingsManager.getConfig().getStringList("patcher.enderpearls.factions-whitelist");

		}
	}
}