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
import org.originmc.fbasics.database.SetupDatabaseTask;
import org.originmc.fbasics.database.UpdateDatabaseTask;
import org.originmc.fbasics.patches.*;
import org.originmc.fbasics.settings.*;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBasics extends JavaPlugin {

    public Connection connection = null;
	public Permission permission = null;
	public Economy economy = null;
	public SettingsManager settingsManager = new SettingsManager(this);
    public Map<String, Integer> crates = new HashMap<String, Integer>();
    public List<String> updateCrates = new ArrayList<String>();


	public void onEnable() {
		getLogger().info("Initiating load...");

		long time = System.currentTimeMillis();
		setupVault();
		setupSettings();
		setupListeners();
		setupCommands();
		time = System.currentTimeMillis() - time;

		getLogger().info("Plugin loaded successfully! (Took " + time + "ms)");
	}


	public void onDisable() {
        if (CrateSettings.enabled) new UpdateDatabaseTask(this).run();
    }


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
            if (PatchSettings.sugarcaneEnabled) {
                getServer().getPluginManager().registerEvents(new SugarcanePatch(), this);
            }
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

        if (PatchSettings.dispenserEnabled) {
            getServer().getPluginManager().registerEvents(new DispenserPatch(), this);
        }

		if (PatchSettings.boatEnabled) {
			getServer().getPluginManager().registerEvents(new BoatPatch(), this);
		}

        if (PatchSettings.mcmmoEnabled) {
            getServer().getPluginManager().registerEvents(new McMMOPatch(), this);
        }

		if (PatchSettings.netherEnabled) {
			getServer().getPluginManager().registerEvents(new NetherRoofPatch(), this);
		}
	}


	private void setupCommands() {

		getCommand("fbasics").setExecutor(new FBCommand(this));

		if (CrateSettings.enabled) {
			getCommand("crate").setExecutor(new CrateCommand(this));
            new SetupDatabaseTask(this).runTaskAsynchronously(this);
            new UpdateDatabaseTask(this).runTaskTimerAsynchronously(this, 6000, 6000);
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
		PatchSettings.loadPatchSettings();
        LanguageSettings.loadLanguageSettings();
		SPSettings.loadSafePromoteSettings();
		WildSettings.loadWildernessSettings();
	}
}