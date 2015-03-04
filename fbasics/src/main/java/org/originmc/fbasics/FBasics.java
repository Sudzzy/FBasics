package org.originmc.fbasics;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.cmd.CmdCrate;
import org.originmc.fbasics.cmd.CmdSafePromote;
import org.originmc.fbasics.cmd.CmdWilderness;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.cmd.CmdFBasics;
import org.originmc.fbasics.entity.CommandEditor;
import org.originmc.fbasics.entity.FBPlayer;
import org.originmc.fbasics.listeners.*;
import org.originmc.fbasics.hooks.factions.FactionsHook;
import org.originmc.fbasics.hooks.factions.FactionsManager;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FBasics extends JavaPlugin {

    private final static Charset ENCODING = StandardCharsets.UTF_8;
    private final Path path = Paths.get(getDataFolder() + File.separator + "db.csv");
    private Economy economy;
    private FactionsHook factionsHook;
    private FileConfiguration config;
    private FileConfiguration language;
    private FileConfiguration materials;
    private Permission permission;

    @Override
    public void onEnable() {
        PluginManager pluginManager = getServer().getPluginManager();
        ServicesManager servicesManager = getServer().getServicesManager();

        try {
            RegisteredServiceProvider<Permission> permissionProvider = servicesManager.getRegistration(Permission.class);
            RegisteredServiceProvider<Economy> economyProvider = servicesManager.getRegistration(Economy.class);

            if (permissionProvider != null) {
                this.permission = permissionProvider.getProvider();
            } else {
                getLogger().severe("Could not find permissions support!");
            }

            if (economyProvider != null) {
                this.economy = economyProvider.getProvider();
            } else {
                getLogger().severe("Could not find economy support!");
            }
        } catch(NoClassDefFoundError error) {
            getLogger().severe("Could not find Vault!");
        }

        this.config = getFileConfiguration("config");
        this.language = getFileConfiguration("language");
        this.materials = getFileConfiguration("materials");

        if (pluginManager.getPlugin("Factions") != null) {
            String factionsVersion = pluginManager.getPlugin("Factions").getDescription().getVersion();
            this.factionsHook = new FactionsManager(factionsVersion).getHook();
        }

        loadDatabase();

        new AntiLootStealListener(this);
        new AntiPhaseListener(this);
        new BoatMovementListener(this);
        new BookLimiterListener(this);
        new ChestDupeListener(this);
        new CommandListener(this);
        new CropDupeListener(this);
        new DismountListener(this);
        new DispenserListener(this);
        new EnderpearlListener(this);
        new McMMODupeListener(this);
        new NetherTeleportListener(this);
        new CmdCrate(this);
        new CmdFBasics(this);
        new CmdSafePromote(this);
        new CmdWilderness(this);
        new SessionListener(this);
    }

    @Override
    public void onDisable() {
        saveDatabase();
    }

    private FileConfiguration getFileConfiguration(String fileName) {
        File file = new File(getDataFolder(), fileName + ".yml");
        FileConfiguration fileConfiguration = new YamlConfiguration();

        try {
            fileConfiguration.load(file);
            String version = fileConfiguration.getString("version");

            if (version != null && version.equals(getDescription().getVersion())) {
                return fileConfiguration;
            }

            if (version == null) {
                version = "backup";
            }

            if (file.renameTo(new File(getDataFolder(), "old-" + fileName + "-" + version + ".yml"))) {
                getLogger().info("Created a backup for: " + fileName + ".yml");
            }

        } catch (IOException|InvalidConfigurationException e) {
            getLogger().info("Generating fresh configuration file: " + fileName + ".yml");
        }

        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                InputStream in = getResource(fileName + ".yml");
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
                out.close();
                in.close();
            }
            fileConfiguration.load(file);
        } catch(IOException|InvalidConfigurationException ex) {
            getLogger().severe("Plugin unable to write configuration file " + fileName + ".yml!");
            getLogger().severe("Disabling...");
            getServer().getPluginManager().disablePlugin(this);
            ex.printStackTrace();
        }

        return fileConfiguration;
    }

    private void loadDatabase() {
        try {
            File file = new File(getDataFolder() + File.separator + "db.csv");
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try (BufferedReader reader = Files.newBufferedReader(this.path, ENCODING)) {
            for (FBPlayer fbPlayer : FBPlayer.getFbPlayers()) {
                fbPlayer.remove();
            }

            String line;

            while ((line = reader.readLine()) != null) {
                new FBPlayer(line);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (FBPlayer.get(uuid) == null) {
                    new FBPlayer(player.getUniqueId().toString() + "," + player.getName());
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveDatabase() {
        try (BufferedWriter writer = Files.newBufferedWriter(this.path, ENCODING)) {
            for (FBPlayer fbPlayer : FBPlayer.getFbPlayers()) {

                for (CommandEditor commandEditor : fbPlayer.getCooldowns().keySet()) {
                    int difference = (int) (System.currentTimeMillis() - fbPlayer.getCooldown(commandEditor)) / 1000;

                    if (difference > commandEditor.getCooldown()) {
                        fbPlayer.removeCooldown(commandEditor);
                    }
                }

                if (!fbPlayer.getCooldowns().isEmpty() && fbPlayer.getCrates() != 0) {
                    writer.write(fbPlayer.toString());
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public FileConfiguration getConfig() {
        return this.config;
    }

    public FileConfiguration getLanguage() {
        return this.language;
    }

    public FileConfiguration getMaterials() {
        return this.materials;
    }

    public Economy getEconomy() {
        return this.economy;
    }

    public Permission getPermission() {
        return this.permission;
    }

    public FactionsHook getFactionsHook() {
        return factionsHook;
    }
}
