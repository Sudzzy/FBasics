package org.originmc.fbasics.settings;

import java.util.List;

public class SPSettings {

	public static boolean enabled;
	public static boolean autoComplete;
	public static List<String> failed;
	public static List<String> success;


	public static void loadSafePromoteSettings() {

		enabled = SettingsManager.getConfig().getBoolean("safe-promote.enabled");


		if (!enabled) {
			return;
		}


		autoComplete = SettingsManager.getConfig().getBoolean("safe-promote.auto-complete-names");
		failed = SettingsManager.getConfig().getStringList("safe-promote.failed-commands");
		success = SettingsManager.getConfig().getStringList("safe-promote.success-commands");

	}
}