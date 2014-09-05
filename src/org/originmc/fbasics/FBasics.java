package org.originmc.fbasics;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.commands.CrateCommand;
import org.originmc.fbasics.commands.FBCommand;
import org.originmc.fbasics.commands.SPCommand;
import org.originmc.fbasics.commands.WildCommand;
import org.originmc.fbasics.database.DatabaseManager;
import org.originmc.fbasics.patches.CactusPatch;
import org.originmc.fbasics.patches.CommandPatch;
import org.originmc.fbasics.patches.AntiLooterPatch;
import org.originmc.fbasics.patches.DismountPatch;
import org.originmc.fbasics.patches.EnderpearlPatch;
import org.originmc.fbasics.patches.BoatPatch;
import org.originmc.fbasics.patches.NetherRoofPatch;
import org.originmc.fbasics.settings.CommandSettings;
import org.originmc.fbasics.settings.CrateSettings;
import org.originmc.fbasics.settings.DatabaseSettings;
import org.originmc.fbasics.settings.LanguageSettings;
import org.originmc.fbasics.settings.PatchSettings;
import org.originmc.fbasics.settings.SPSettings;
import org.originmc.fbasics.settings.SettingsManager;
import org.originmc.fbasics.settings.WildSettings;

public class FBasics extends JavaPlugin {

	public Permission permission = null;
	public Economy economy = null;
	public SettingsManager settingsManager = new SettingsManager(this);
	public DatabaseManager databaseManager = new DatabaseManager(this);


	public void onEnable() {
		getLogger().info("Initiating load...");

		long time = System.currentTimeMillis();
		setupVault();
		setupSettings();
		setupListeners();
		setupCommands();
		this.databaseManager.setupConnection();
		this.databaseManager.createTables();
		time = System.currentTimeMillis() - time;

		getLogger().info("Plugin loaded successfully! (Took " + time + "ms)");
	}


	public void onDisable() { }


	private void setupVault() {
		if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
			getLogger().warning("##############################");
			getLogger().warning("Vault not found!");
			getLogger().warning("Permission and Economy hooks disabled.");
			getLogger().warning("##############################");
			return;
		}

		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(Permission.class);
		this.permission = permissionProvider.getProvider();

		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
		this.economy = economyProvider.getProvider();
	}


	private void setupListeners() {

		if (PatchSettings.antiLooterEnabled) {
			getServer().getPluginManager().registerEvents(new AntiLooterPatch(this), this);
		}

		if (PatchSettings.cactusEnabled) {
			getServer().getPluginManager().registerEvents(new CactusPatch(), this);
		}
		
		if (CommandSettings.enabled) {
			getServer().getPluginManager().registerEvents(new CommandPatch(this), this);
		}

		if (PatchSettings.dismountEnabled) {
			getServer().getPluginManager().registerEvents(new DismountPatch(this), this);
		}

		if (PatchSettings.enderpearlsEnabled) {
			getServer().getPluginManager().registerEvents(new EnderpearlPatch(this), this);
		}

		if (PatchSettings.boatEnabled) {
			getServer().getPluginManager().registerEvents(new BoatPatch(), this);
		}

		if (PatchSettings.netherEnabled) {
			getServer().getPluginManager().registerEvents(new NetherRoofPatch(), this);
		}

	}


	private void setupCommands() {

		getCommand("fbasics").setExecutor(new FBCommand(this));

		if (CrateSettings.enabled) {
			getCommand("crate").setExecutor(new CrateCommand(this));
		}

		if (SPSettings.enabled && this.permission != null) {
			getCommand("safepromote").setExecutor(new SPCommand(this));
		}

		if (WildSettings.enabled) {
			getCommand("wilderness").setExecutor(new WildCommand(this));
		}

	}


	private void setupSettings() {
		this.settingsManager.getFiles();
		this.settingsManager.updateFiles();
		CommandSettings.loadCommandSettings();
		CrateSettings.loadCrateSettings();
		DatabaseSettings.loadCommandSettings();
		PatchSettings.loadPatchSettings();
		LanguageSettings.loadLanguageSettings();
		SPSettings.loadSafePromoteSettings();
		WildSettings.loadWildernessSettings();
	}

}