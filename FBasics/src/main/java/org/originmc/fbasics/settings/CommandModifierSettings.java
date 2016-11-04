package org.originmc.fbasics.settings;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.regex.Pattern;

@Data
@EqualsAndHashCode(callSuper = true)
public final class CommandModifierSettings extends CommandModifierGroupSettings implements ISettings {

    private static final String REGEX = "regex";

    private static final String GROUPS = "groups.";

    private final String name;

    private final String path;

    private final FBasics plugin;

    private ConfigurationSection configuration;

    private Pattern regex = Pattern.compile("");

    private HashMap<String, CommandModifierGroupSettings> groups = new HashMap<>();

    public CommandModifierSettings(FBasics plugin, String path, String name) {
        super(plugin, null, null, path);
        this.name = name;
        this.path = path;
        this.plugin = plugin;
    }

    @Override
    public void load() {
        configuration = plugin.getConfig().getConfigurationSection(path);
        super.load();
        regex = Pattern.compile(configuration.getString(REGEX, ""));

        // Do nothing if groups are not defined.
        if (!configuration.contains(GROUPS)) {
            groups.clear();
            return;
        }

        // Create a sorted list of group names dependant of weight.
        ArrayList<String> groupNames = new ArrayList<>();
        ConfigurationSection configuration = this.configuration.getConfigurationSection(GROUPS);
        for (String groupName : configuration.getKeys(false)) {
            groupNames.add(groupName.toLowerCase());
        }
        Collections.sort(groupNames, new GroupComparator());

        // Remove all unused groups.
        for (String groupName : this.groups.keySet()) {
            if (!groupNames.contains(groupName)) {
                this.groups.remove(groupName);
            }
        }

        // Create all groups in order of weight.
        ArrayList<CommandModifierGroupSettings> groups = new ArrayList<>();
        groups.add(this);
        for (String groupName : groupNames) {
            // Attempt to load an already cached group settings, otherwise create a new one.
            CommandModifierGroupSettings group = this.groups.get(groupName);
            if (group == null) {
                group = new CommandModifierGroupSettings(plugin, this, groupName, configuration.getConfigurationSection(groupName).getCurrentPath());
            }

            // Load the groups' settings.
            groups.add(group);
            group.setPredecessors(groups.toArray(new CommandModifierGroupSettings[groups.size()]));
            group.load();

            // Cache group.
            this.groups.put(groupName, group);
        }
    }

    private class GroupComparator implements Comparator<String> {

        @Override
        public int compare(String group1, String group2) {
            return Integer.compare(configuration.getInt(group1 + ".weight"), configuration.getInt(group2 + ".weight"));
        }

    }

}
