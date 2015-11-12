package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;

@Data
public final class WildernessWorldSettings implements ISettings {

    private static final String CENTER_X = "center-x";

    private static final String CENTER_Z = "center-z";

    private static final String RANGE = "range";

    private final FBasics plugin;

    private final String path;

    private ConfigurationSection configuration;

    private int centerX = 0;

    private int centerZ = 0;

    private int range = 0;

    @Override
    public void load() {
        configuration = plugin.getConfig().getConfigurationSection(path);
        centerX = configuration.getInt(CENTER_X, 0);
        centerZ = configuration.getInt(CENTER_Z, 0);
        range = configuration.getInt(RANGE, 0);
    }

}
