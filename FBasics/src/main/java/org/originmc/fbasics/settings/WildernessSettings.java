package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.originmc.fbasics.util.SettingsUtils.getMaterialList;

@Data
public final class WildernessSettings implements ISettings {

    private static final String PREFIX = "wilderness-";

    private static final String ENABLED = PREFIX + "enabled";

    private static final String ATTEMPTS = PREFIX + "attempts";

    private static final String FACTIONS_RADIUS = PREFIX + "factions-radius";

    private static final String FACTIONS_ASYNCHRONOUS_SEARCH = PREFIX + "factions-asynchronous-search";

    private static final String DEFAULT_WORLD = PREFIX + "default-world";

    private static final String SEARCH_MESSAGE = PREFIX + "search-message";

    private static final String SUCCESS_MESSAGE = PREFIX + "success-message";

    private static final String ATTEMPTS_MESSAGE = PREFIX + "attempts-message";

    private static final String WORLD_MESSAGE = PREFIX + "world-message";

    private static final String DENIED_BLOCKS = PREFIX + "denied-blocks";

    private static final String WORLDS = PREFIX + "worlds";

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private boolean enabled = false;

    private int attempts = 0;

    private int factionsRadius = 0;

    private boolean factionsAsynchronousSearch = false;

    private String defaultWorld = "";

    private String searchMessage = "";

    private String successMessage = "";

    private String attemptsMessage = "";

    private String worldMessage = "";

    private List<Material> deniedBlocks = new ArrayList<>();

    private HashMap<String, WildernessWorldSettings> worlds = new HashMap<>();

    @Override
    public void load() {
        configuration = plugin.getConfig();
        enabled = configuration.getBoolean(ENABLED, false);
        attempts = configuration.getInt(ATTEMPTS, 0);
        factionsRadius = configuration.getInt(FACTIONS_RADIUS, 0);
        factionsAsynchronousSearch = configuration.getBoolean(FACTIONS_ASYNCHRONOUS_SEARCH, false);
        defaultWorld = configuration.getString(DEFAULT_WORLD, "");
        searchMessage = configuration.getString(SEARCH_MESSAGE, "");
        successMessage = configuration.getString(SUCCESS_MESSAGE, "");
        attemptsMessage = configuration.getString(ATTEMPTS_MESSAGE, "");
        worldMessage = configuration.getString(WORLD_MESSAGE, "");
        deniedBlocks = getMaterialList(configuration.getStringList(DENIED_BLOCKS));
        worlds.clear();

        if (configuration.contains(WORLDS)) {
            ConfigurationSection configuration = this.configuration.getConfigurationSection(WORLDS);
            for (String worldName : configuration.getKeys(false)) {
                WildernessWorldSettings world = new WildernessWorldSettings(plugin, configuration.getConfigurationSection(worldName).getCurrentPath());
                world.load();
                worlds.put(worldName, world);
            }
        }
    }

}
