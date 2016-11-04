package org.originmc.fbasics.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.factions.api.FactionsMode;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;

@Data
@EqualsAndHashCode(exclude = "predecessors")
public class CommandModifierGroupSettings implements ISettings {

    private static final String ALIAS = "alias";

    private static final String COOLDOWN = "cooldown";

    private static final String WARMUP = "warmup";

    private static final String WARMUP_CANCEL_ON_DAMAGE = "warmup-cancel-on-damage";

    private static final String WARMUP_CANCEL_ON_MOVE = "warmup-cancel-on-move";

    private static final String PRICE = "price";

    private static final String PERMISSION = "permission";

    private static final String PERMISSION_MESSAGE = "permission-message";

    private static final String MESSAGE = "message";

    private static final String FACTIONS = "factions";

    private static final String FACTIONS_MODE = "factions-mode";

    private final FBasics plugin;

    private final CommandModifierSettings root;

    private final String group;

    private final String path;

    private ConfigurationSection configuration;

    private CommandModifierGroupSettings[] predecessors = new CommandModifierGroupSettings[]{this};

    private String alias = "";

    private long cooldown = 0;

    private long warmup = 0;

    private boolean warmupCancelOnDamage = false;

    private boolean warmupCancelOnMove = false;

    private double price = 0.0;

    private String permission = "";

    private String permissionMessage = "";

    private String message = "";

    private List<String> factions = new ArrayList<>();

    private FactionsMode factionsMode = FactionsMode.BLACKLIST;

    @Override
    public void load() {
        configuration = plugin.getConfig().getConfigurationSection(path);

        // Load all configuration settings that have actually been defined.
        for (CommandModifierGroupSettings predecessor : predecessors) {
            ConfigurationSection configuration = predecessor.getConfiguration();
            if (configuration.contains(ALIAS)) {
                alias = configuration.getString(ALIAS, "");
            }

            if (configuration.contains(COOLDOWN)) {
                cooldown = SECONDS.toMillis(configuration.getInt(COOLDOWN, 0));
            }

            if (configuration.contains(WARMUP)) {
                warmup = SECONDS.toMillis(configuration.getInt(WARMUP, 0));
            }

            if (configuration.contains(WARMUP_CANCEL_ON_DAMAGE)) {
                warmupCancelOnDamage = configuration.getBoolean(WARMUP_CANCEL_ON_DAMAGE, false);
            }

            if (configuration.contains(WARMUP_CANCEL_ON_MOVE)) {
                warmupCancelOnMove = configuration.getBoolean(WARMUP_CANCEL_ON_MOVE, false);
            }

            if (configuration.contains(PRICE)) {
                price = configuration.getDouble(PRICE, 0);
            }

            if (configuration.contains(PERMISSION)) {
                permission = configuration.getString(PERMISSION, "");
            }

            if (configuration.contains(PERMISSION_MESSAGE)) {
                permissionMessage = configuration.getString(PERMISSION_MESSAGE, "");
            }

            if (configuration.contains(MESSAGE)) {
                message = configuration.getString(MESSAGE, "");
            }

            if (configuration.contains(FACTIONS)) {
                factions = configuration.getStringList(FACTIONS);
            }

            if (configuration.contains(FACTIONS_MODE)) {
                factionsMode = FactionsMode.getFactionsMode(configuration.getString(FACTIONS_MODE, ""), FactionsMode.BLACKLIST);
            }
        }
    }

}
