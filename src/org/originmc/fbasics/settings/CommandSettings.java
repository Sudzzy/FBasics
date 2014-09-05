package org.originmc.fbasics.settings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommandSettings {

	public static boolean enabled;
	public static boolean ignoreCase;
	public static String priority;
	public static Set<String> editors;
	public static Map<String, String> matcher;
	public static Map<String, String> alias;
	public static Map<String, String> permission;
	public static Map<String, Double> price;
	public static Map<String, Integer> warmup;
	public static Map<String, Integer> cooldown;
	public static Map<String, List<String>> blocks;
	public static Map<String, List<String>> factions;


	public static void loadCommandSettings() {

		enabled = SettingsManager.getConfig().getBoolean("commands.enabled");


		if (!enabled) {
			return;
		}


		priority = SettingsManager.getConfig().getString("commands.priority");
		ignoreCase = SettingsManager.getConfig().getBoolean("commands.ignore-case");
		editors = SettingsManager.getConfig().getConfigurationSection("commands.editors").getKeys(false);


		Map<String, String> commandsMatcher = new HashMap<String, String>();
		Map<String, String> commandsAlias = new HashMap<String, String>();
		Map<String, String> commandsPermission = new HashMap<String, String>();
		Map<String, Double> commandsPrice = new HashMap<String, Double>();
		Map<String, Integer> commandsWarmup = new HashMap<String, Integer>();
		Map<String, Integer> commandsCooldown = new HashMap<String, Integer>();
		Map<String, List<String>> commandsBlocks = new HashMap<String, List<String>>();
		Map<String, List<String>> commandsFactions = new HashMap<String, List<String>>();


		for (String editor : editors) {
			commandsMatcher.put(SettingsManager.getConfig().getString("commands.editors." + editor + ".match"), editor);
			commandsAlias.put(editor, SettingsManager.getConfig().getString("commands.editors." + editor + ".alias"));
			commandsPermission.put(editor, SettingsManager.getConfig().getString("commands.editors." + editor + ".permission"));
			commandsPrice.put(editor, SettingsManager.getConfig().getDouble("commands.editors." + editor + ".price"));
			commandsWarmup.put(editor, SettingsManager.getConfig().getInt("commands.editors." + editor + ".warmup"));
			commandsCooldown.put(editor, SettingsManager.getConfig().getInt("commands.editors." + editor + ".cooldown"));
			commandsBlocks.put(editor, SettingsManager.getConfig().getStringList("commands.editors." + editor + ".blocks"));
			commandsFactions.put(editor, SettingsManager.getConfig().getStringList("commands.editors." + editor + ".factions"));
		}


		matcher = commandsMatcher;
		alias = commandsAlias;
		permission = commandsPermission;
		price = commandsPrice;
		warmup = commandsWarmup;
		cooldown = commandsCooldown;
		blocks = commandsBlocks;
		factions = commandsFactions;

	}
}