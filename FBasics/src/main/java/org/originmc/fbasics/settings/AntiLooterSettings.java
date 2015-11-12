package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;

import static java.util.concurrent.TimeUnit.SECONDS;

@Data
public final class AntiLooterSettings implements ISettings {

    private static final String PREFIX = "antilooter-";

    private static final String ENABLED = PREFIX + "enabled";

    private static final String DURATION = PREFIX + "duration";

    private static final String DROPPED_MESSAGE = PREFIX + "dropped-message";

    private static final String PROTECTED_MESSAGE = PREFIX + "protected-message";

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private boolean enabled = false;

    private long duration = 0;

    private String droppedMessage = "";

    private String protectedMessage = "";

    @Override
    public void load() {
        configuration = plugin.getConfig();
        enabled = configuration.getBoolean(ENABLED, false);
        duration = SECONDS.toMillis(configuration.getInt(DURATION, 0));
        droppedMessage = configuration.getString(DROPPED_MESSAGE, "");
        protectedMessage = configuration.getString(PROTECTED_MESSAGE, "");
    }

}
