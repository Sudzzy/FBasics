package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.List;

@Data
public final class SafePromoteSettings implements ISettings {

    private static final String PREFIX = "safepromote-";

    private static final String ENABLED = PREFIX + "enabled";

    private static final String AUTOCOMPLETE = PREFIX + "autocomplete";

    private static final String FAILED_COMMANDS = PREFIX + "failed-commands";

    private static final String SUCCESS_COMMANDS = PREFIX + "success-commands";

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private boolean enabled = false;

    private boolean autocomplete = false;

    private List<String> failedCommands = new ArrayList<>();

    private List<String> successCommands = new ArrayList<>();

    @Override
    public void load() {
        configuration = plugin.getConfig();
        enabled = configuration.getBoolean(ENABLED);
        autocomplete = configuration.getBoolean(AUTOCOMPLETE, false);
        failedCommands = configuration.getStringList(FAILED_COMMANDS);
        successCommands = configuration.getStringList(SUCCESS_COMMANDS);
    }

}
