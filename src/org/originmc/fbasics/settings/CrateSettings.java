package org.originmc.fbasics.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrateSettings {

	public static boolean enabled;
	public static boolean algorithm;
	public static Set<String> rewards;
	public static Map<String, String> rewardMessages;
	public static Map<String, List<String>> rewardCommands;


	public static void loadCrateSettings() {

		enabled = SettingsManager.getConfig().getBoolean("crates.enabled");


		if (!enabled) {
			return;
		}


		algorithm = SettingsManager.getConfig().getBoolean("crates.new-reward-algorithm");
		rewards = SettingsManager.getConfig().getConfigurationSection("crates.rewards").getKeys(false);

		String messagePrefix = SettingsManager.getConfig().getString("crates.message-prefix");

		Map<String, List<String>> cratesCommands = new HashMap<String, List<String>>();
		Map<String, String> cratesMessages = new HashMap<String, String>();


		for (String reward : rewards) {
			cratesCommands.put(reward, SettingsManager.getConfig().getStringList("crates.rewards." + reward + ".commands"));
			cratesMessages.put(reward, messagePrefix + SettingsManager.getConfig().getString("crates.rewards." + reward + ".message"));
		}


		rewardCommands = cratesCommands;
		rewardMessages = cratesMessages;


		cratesCommands = null;
		cratesMessages = null;

	}
}