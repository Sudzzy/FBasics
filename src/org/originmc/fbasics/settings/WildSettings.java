package org.originmc.fbasics.settings;

import java.util.List;

public class WildSettings {

	public static boolean enabled;
	public static int xCenter;
	public static int zCenter;
	public static int maxRange;
	public static int minRange;
	public static List<String> worlds;
	public static List<String> blocks;


	public static void loadWildernessSettings() {

		enabled = SettingsManager.getConfig().getBoolean("wilderness.enabled");


		if (!enabled) {
			return;
		}


		xCenter = SettingsManager.getConfig().getInt("wilderness.x-center");
		zCenter = SettingsManager.getConfig().getInt("wilderness.z-center");
		maxRange = SettingsManager.getConfig().getInt("wilderness.max-range");
		minRange = SettingsManager.getConfig().getInt("wilderness.min-range");
		worlds = SettingsManager.getConfig().getStringList("wilderness.whitelisted-worlds");
		blocks = SettingsManager.getConfig().getStringList("wilderness.disabled-blocks");
		
	}
}