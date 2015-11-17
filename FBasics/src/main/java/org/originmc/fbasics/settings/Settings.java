package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.factions.api.FactionsVersion;
import org.originmc.fbasics.task.CleanDatabaseTask;
import org.originmc.fbasics.task.SaveDatabaseTask;

@Data
public final class Settings implements ISettings {

    private static final String CONFIG_VERSION = "config-version";

    private static final String DATABASE_CLEAN = "database-clean";

    private static final String DATABASE_SAVE = "database-save";

    private static final String FACTIONS_VERSION = "factions-version";

    private static final String UPDATE_CONFIG = "update-config";

    private final FBasics plugin;

    private final AntiGlitchSettings antiGlitchSettings;

    private final AntiLooterSettings antiLooterSettings;

    private final CommandSettings commandSettings;

    private final SafePromoteSettings safePromoteSettings;

    private final WildernessSettings wildernessSettings;

    private ConfigurationSection configuration;

    private int configVersion = 0;

    private int databaseClean = 0;

    private int databaseSave = 0;

    private FactionsVersion factionsVersion;

    private boolean updateConfig = false;

    private int cleanDatabaseTaskId = 0;

    private int saveDatabaseTaskId = 0;

    public Settings(FBasics plugin) {
        this.plugin = plugin;
        antiGlitchSettings = new AntiGlitchSettings(plugin);
        antiLooterSettings = new AntiLooterSettings(plugin);
        commandSettings = new CommandSettings(plugin);
        safePromoteSettings = new SafePromoteSettings(plugin);
        wildernessSettings = new WildernessSettings(plugin);
    }

    @Override
    public void load() {
        plugin.reloadConfig();
        configuration = plugin.getConfig();
        configVersion = configuration.getInt(CONFIG_VERSION, 0);
        databaseClean = configuration.getInt(DATABASE_CLEAN, 0) * 20;
        databaseSave = configuration.getInt(DATABASE_SAVE, 0) * 20;
        factionsVersion = FactionsVersion.parse(configuration.getString(FACTIONS_VERSION, "auto"));
        updateConfig = configuration.getBoolean(UPDATE_CONFIG, false);
        antiGlitchSettings.load();
        antiLooterSettings.load();
        commandSettings.load();
        safePromoteSettings.load();
        wildernessSettings.load();

        // Cancel all active tasks.
        if (cleanDatabaseTaskId != 0) {
            Bukkit.getScheduler().cancelTask(cleanDatabaseTaskId);
        }

        if (saveDatabaseTaskId != 0) {
            Bukkit.getScheduler().cancelTask(saveDatabaseTaskId);
        }

        // Schedule database cleaning.
        if (databaseClean > 0) {
            cleanDatabaseTaskId = Bukkit.getScheduler().runTaskTimer(plugin,
                    new CleanDatabaseTask(plugin), databaseClean, databaseClean).getTaskId();
        } else {
            cleanDatabaseTaskId = 0;
        }

        // Schedule database saving.
        if (databaseSave > 0) {
            saveDatabaseTaskId = Bukkit.getScheduler().runTaskTimer(plugin,
                    new SaveDatabaseTask(plugin), databaseSave, databaseSave).getTaskId();
        } else {
            saveDatabaseTaskId = 0;
        }
    }

}
