package org.originmc.fbasics.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventPriority;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SettingsUtils {

    /**
     * Converts a string into an event priority.
     *
     * @param str the string to convert.
     * @return settings as an event priority.
     */
    public static EventPriority getEventPriority(String str) {
        EventPriority priority;
        try {
            priority = EventPriority.valueOf(str.toUpperCase());
        } catch (Exception e) {
            priority = EventPriority.NORMAL;
        }
        return priority;
    }

    /**
     * Converts a string list into a material list.
     *
     * @param settings the list of materials in string form to load.
     * @return settings as a material list.
     */
    public static List<Material> getMaterialList(List<String> settings) {
        List<Material> materials = new ArrayList<>();
        for (String setting : settings) {
            setting = setting.toUpperCase();
            try {
                materials.add(Material.valueOf(setting));
            } catch (Exception e) {
                Bukkit.getLogger().warning("[FBasics] Invalid material " + setting);
            }
        }
        return materials;
    }

    /**
     * Creates the parent directories and a blank file for this file.
     *
     * @param file the file to initialize.
     * @throws IOException
     */
    public static void initialize(File file) throws IOException {
        file.getParentFile().mkdirs();
        file.createNewFile();
    }

}
