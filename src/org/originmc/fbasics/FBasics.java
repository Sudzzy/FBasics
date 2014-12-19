package org.originmc.fbasics;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.cmd.CmdCrate;
import org.originmc.fbasics.cmd.CmdFBasics;
import org.originmc.fbasics.cmd.CmdSafePromote;
import org.originmc.fbasics.cmd.CmdWilderness;
import org.originmc.fbasics.listeners.*;
import org.originmc.fbasics.task.SetupDatabaseTask;
import org.originmc.fbasics.task.UpdateDatabaseTask;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FBasics extends JavaPlugin {

    public Connection connection;
    public List<String> updateCrates = new ArrayList<String>();
    public Map<String, Integer> crates = new HashMap<String, Integer>();

    private Economy economy;
    private FileConfiguration config;
    private FileConfiguration language;
    private FileConfiguration materials;
    private Permission permission;
    private String factionsVersion = "";

    @Override
    public void onEnable() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        ServicesManager servicesManager = this.getServer().getServicesManager();
        RegisteredServiceProvider<Economy> economyProvider = servicesManager.getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);

        this.economy = economyProvider.getProvider();
        this.permission = permissionProvider.getProvider();
        this.config = new ConfigManager(this, "config").getConfig();
        this.language = new ConfigManager(this, "language").getConfig();
        this.materials = new ConfigManager(this, "materials").getConfig();

        if (pluginManager.getPlugin("Factions") != null) {
            this.factionsVersion = pluginManager.getPlugin("Factions").getDescription().getVersion();
        }

        if (this.config.getBoolean("anti-looter.enabled")) {
            pluginManager.registerEvents(new AntiLootStealListener(this), this);
        }

        if (this.config.getBoolean("commands.enabled")) {
            pluginManager.registerEvents(new CommandListener(this), this);
        }

        if (this.config.getBoolean("patcher.anti-phase")) {
            pluginManager.registerEvents(new AntiPhaseListener(this), this);
        }

        if (this.config.getBoolean("patcher.boat-glitch")) {
            pluginManager.registerEvents(new BoatMovementListener(), this);
        }

        if (this.config.getBoolean("patcher.chest-dupe")) {
            pluginManager.registerEvents(new ChestDupeListener(this), this);
        }

        if (this.config.getBoolean("patcher.crop-dupe")) {
            pluginManager.registerEvents(new CropDupeListener(this), this);
        }

        if (this.config.getBoolean("patcher.dismount-glitch")) {
            pluginManager.registerEvents(new DismountListener(this), this);
        }

        if (this.config.getBoolean("patcher.dispenser-glitch")) {
            pluginManager.registerEvents(new DispenserListener(), this);
        }

        if (this.config.getBoolean("patcher.enderpearls.enabled")) {
            pluginManager.registerEvents(new EnderpearlListener(this), this);
        }

        if (this.config.getBoolean("patcher.mcmmo-mining-exploit")) {
            pluginManager.registerEvents(new McMMODupeListener(this), this);
        }

        if (this.config.getBoolean("patcher.nether-glitch")) {
            pluginManager.registerEvents(new NetherTeleportListener(this), this);
        }

        if (this.config.getBoolean("crates.enabled")) {
            getCommand("crate").setExecutor(new CmdCrate(this));
            new SetupDatabaseTask(this).runTaskAsynchronously(this);
            new UpdateDatabaseTask(this).runTaskTimerAsynchronously(this, 6000, 6000);
        }

        getCommand("fbasics").setExecutor(new CmdFBasics(this));

        if (this.config.getBoolean("safe-promote.enabled")) {
            getCommand("safepromote").setExecutor(new CmdSafePromote(this));
        }

        if (this.config.getBoolean("wilderness.enabled")) {
            getCommand("wilderness").setExecutor(new CmdWilderness(this));
        }
    }

    @Override
    public void onDisable() {
        if (this.config.getBoolean("crates.enabled")) new UpdateDatabaseTask(this).run();
    }

    @Override
    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getLanguage() {
        return this.language;
    }

    public FileConfiguration getMaterials() {
        return this.materials;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public String getFactionsVersion() {
        return this.factionsVersion;
    }
}
