package org.originmc.fbasics;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.originmc.fbasics.cmd.CmdCrate;
import org.originmc.fbasics.cmd.CmdSafePromote;
import org.originmc.fbasics.cmd.CmdWilderness;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.cmd.CmdFBasics;
import org.originmc.fbasics.task.UpdateDatabaseTask;
import org.originmc.fbasics.listeners.*;
import org.originmc.hooks.factions.FactionsHook;
import org.originmc.hooks.factions.FactionsManager;

import java.io.*;
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
    private FactionsHook factionsHook;
    private FileConfiguration config;
    private FileConfiguration language;
    private FileConfiguration materials;
    private Permission permission;

    @Override
    public void onEnable() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        ServicesManager servicesManager = this.getServer().getServicesManager();
        RegisteredServiceProvider<Economy> economyProvider = servicesManager.getRegistration(Economy.class);
        RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);

        this.economy = economyProvider.getProvider();
        this.permission = permissionProvider.getProvider();
        this.config = getFileConfiguration("config");
        this.language = getFileConfiguration("language");
        this.materials = getFileConfiguration("materials");

        if (pluginManager.getPlugin("Factions") != null) {
            String version = pluginManager.getPlugin("Factions").getDescription().getVersion();
            String error = language.getString("general.error.prefix");
            String msgFaction = error + language.getString("commands.error.faction");
            List<String> factions = config.getStringList("patcher.enderpearls.factions-whitelist");
            this.factionsHook = new FactionsManager(version, msgFaction, factions).getHook();
        }

        new AntiLootStealListener(this);
        new AntiPhaseListener(this);
        new BoatMovementListener(this);
        new BookLimiterListener(this);
        new ChestDupeListener(this);
        new CommandListener(this);
        new CropDupeListener(this);
        new DismountListener(this);
        new DispenserListener(this);
        new EnderpearlListener(this);
        new McMMODupeListener(this);
        new NetherTeleportListener(this);
        new CmdCrate(this);
        new CmdFBasics(this);
        new CmdSafePromote(this);
        new CmdWilderness(this);
    }

    @Override
    public void onDisable() {
        if (this.config.getBoolean("crates.enabled")) {
            new UpdateDatabaseTask(this).run();
        }
    }

    private FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(getDataFolder(), fileName + ".yml");
        FileConfiguration fileConfiguration = new YamlConfiguration();

        try {
            fileConfiguration.load(file);
            String version = fileConfiguration.getString("version");

            if (version != null && version.equals(getDescription().getVersion())) {
                return fileConfiguration;
            }

            if (version == null) {
                version = "backup";
            }

            if (file.renameTo(new File(getDataFolder(), "old-" + fileName + "-" + version + ".yml"))) {
                getLogger().info("Created a backup for: " + fileName + ".yml");
            }

        } catch (Exception e) {
            getLogger().info("Generating fresh configuration file: " + fileName + ".yml");
        }

        try {
            if (!file.exists()) {
                if (file.getParentFile().mkdirs()) {
                    InputStream in = getResource(fileName + ".yml");
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                    out.close();
                    in.close();
                }
            }
            fileConfiguration.load(file);
        } catch(IOException|InvalidConfigurationException ex) {
            getLogger().severe("Plugin unable to write configuration file " + fileName + ".yml!");
            getLogger().severe("Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            ex.printStackTrace();
        }

        return fileConfiguration;
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

    public FactionsHook getFactionsHook() {
        return factionsHook;
    }
}
