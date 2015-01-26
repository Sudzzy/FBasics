package org.originmc.fbasics;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class ConfigManager {

    private FileConfiguration config;

    public ConfigManager(FBasics plugin, String fileName) {
        File file = new File(plugin.getDataFolder(), fileName + ".yml");
        this.config = new YamlConfiguration();

        try {
            this.config.load(file);
            String version = this.config.getString("version");

            if (version != null && version.equals(plugin.getDescription().getVersion())) return;
            if (version == null) version = "backup";

            file.renameTo(new File(plugin.getDataFolder(), "old-" + fileName + "-" + version + ".yml"));
            plugin.getLogger().info("Created a backup for: " + fileName + ".yml");

        } catch (Exception e) {
            plugin.getLogger().info("Generating fresh configuration file: " + fileName + ".yml");
        }

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                InputStream in = plugin.getResource(fileName + ".yml");
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                out.close();
                in.close();
            }
            this.config.load(file);
        } catch (Exception e) {
            plugin.getLogger().severe("Plugin unable to write configuration file " + fileName + ".yml!");
            plugin.getLogger().severe("Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return this.config;
    }
}
