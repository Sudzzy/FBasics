package org.originmc.fbasics.settings;

import lombok.Data;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.factions.api.FactionsMode;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

@Data
public final class AntiGlitchEnderpearlsSettings implements ISettings {

    private static final String PREFIX = "antiglitch-enderpearls-";

    private static final String DISABLE_WITHIN_BLOCK = PREFIX + "disable-within-block";

    private static final String DISABLE_WITHIN_BLOCK_MESSAGE = PREFIX + "disable-within-block-message";

    private static final String CORRECT_TELEPORT = PREFIX + "correct-teleport";

    private static final String COOLDOWN = PREFIX + "cooldown";

    private static final String COOLDOWN_MESSAGE = PREFIX + "cooldown-message";

    private static final String MULTIPLE_MESSAGE = PREFIX + "multiple-message";

    private static final String DOOR_COOLDOWN = PREFIX + "door-cooldown";

    private static final String FACTIONS = PREFIX + "factions";

    private static final String FACTIONS_MODE = PREFIX + "factions-mode";

    private static final String FACTIONS_MESSAGE = PREFIX + "factions-message";

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private boolean disableWithinBlock = false;

    private String disableWithinBlockMessage = "";

    private boolean correctTeleport = false;

    private long cooldown = 0;

    private String cooldownMessage = "";

    private String multipleMessage = "";

    private long doorCooldown = 0;

    private List<String> factions = new ArrayList<>();

    private FactionsMode factionsMode = FactionsMode.BLACKLIST;

    private String factionsMessage = "";

    @Override
    public void load() {
        configuration = plugin.getConfig();
        disableWithinBlock = configuration.getBoolean(DISABLE_WITHIN_BLOCK, false);
        disableWithinBlockMessage = configuration.getString(DISABLE_WITHIN_BLOCK_MESSAGE, "");
        correctTeleport = configuration.getBoolean(CORRECT_TELEPORT, false);
        cooldown = SECONDS.toMillis(configuration.getInt(COOLDOWN, 0));
        cooldownMessage = configuration.getString(COOLDOWN_MESSAGE, "");
        multipleMessage = configuration.getString(MULTIPLE_MESSAGE, "");
        doorCooldown = SECONDS.toMillis(configuration.getInt(DOOR_COOLDOWN, 0));
        factions = configuration.getStringList(FACTIONS);
        factionsMode = FactionsMode.getFactionsMode(configuration.getString(FACTIONS_MODE, ""), FactionsMode.BLACKLIST);
        factionsMessage = configuration.getString(FACTIONS_MESSAGE, "");
    }

}
