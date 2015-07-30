package org.originmc.fbasics;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.cmd.CmdCrate;
import org.originmc.fbasics.cmd.CmdFBasics;
import org.originmc.fbasics.cmd.CmdSafePromote;
import org.originmc.fbasics.cmd.CmdWilderness;
import org.originmc.fbasics.factions.api.FactionsHelper;
import org.originmc.fbasics.factions.api.FactionsHelperImpl;
import org.originmc.fbasics.listeners.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

public class FBasics extends JavaPlugin {

    private final static Charset ENCODING = StandardCharsets.UTF_8;

    private final Path path = Paths.get(getDataFolder() + File.separator + "db.csv");

    private Economy economy;

    private FileConfiguration config;

    private FileConfiguration language;

    private FileConfiguration materials;

    private Permission permission;

    private FactionsManager factionsManager;

    public Economy getEconomy() {
        return this.economy;
    }

    public FactionsManager getFactionsManager() {
        return factionsManager;
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

    public Permission getPermission() {
        return this.permission;
    }

    @Override
    public void onEnable() {
        // Integrate dependencies
        integrateFactions();
        integrateVault();

        // Generate config files
        this.config = getFileConfiguration("config");
        this.language = getFileConfiguration("language");
        this.materials = getFileConfiguration("materials");

        // Load user database
        // TODO: Design a better database system. Preferably supporting all MySQL, SQLite and FlatFile
        loadDatabase();

        // Register listeners
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
        // Save user database
        saveDatabase();
    }

    private void integrateFactions() {
        // Determine if Factions is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
        if (plugin == null) {
            getLogger().info("Factions integration is disabled because it is not loaded");

            // Use the dummy helper implementation if Factions isn't loaded
            factionsManager = new FactionsManager(new FactionsHelperImpl());
            return;
        }

        // Determine which helper class implementation to use
        FactionsHelper helper;
        String[] v = plugin.getDescription().getVersion().split("\\.");
        String version = v[0] + "_" + v[1];

        // Special case for HCF. Use FactionsUUID 1.6 hook
        if (version.compareTo("1_6") < 0) {
            version = "1_6";
        }

        // Determine which hook implementation to use
        String className = "org.originmc.fbasics.factions.v" + version + ".FactionsHelperImpl";

        try {
            // Try to create a new helper instance
            helper = (FactionsHelper) Class.forName(className).newInstance();

            // Create the manager which is what the plugin will interact with
            factionsManager = new FactionsManager(helper);
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible Factions
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors");
            getLogger().warning("This is most likely due to a newer Factions");

            // Use the dummy helper implementation since WG isn't supported
            factionsManager = new FactionsManager(new FactionsHelperImpl());

            // Let's leave a stack trace in console for reporting
            e.printStackTrace();
        }
    }

    private void integrateVault() {
        // Determine if Vault is loaded
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (plugin == null) {
            getLogger().info("Vault integration is disabled as it is not loaded");
            return;
        }

        ServicesManager sm = getServer().getServicesManager();
        RegisteredServiceProvider<Permission> permissionProvider = sm.getRegistration(Permission.class);
        RegisteredServiceProvider<Economy> economyProvider = sm.getRegistration(Economy.class);

        if (permissionProvider != null) {
            this.permission = permissionProvider.getProvider();
        } else {
            // No permissions provider has been found
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Permissions integration!");
            getLogger().warning("This is most likely due Vault not being installed");
            getLogger().warning("Certain features of this plugin may not function correctly");
        }

        if (economyProvider != null) {
            this.economy = economyProvider.getProvider();
        } else {
            // No economy provider has been found
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Economy integration!");
            getLogger().warning("This is most likely due no economy plugin being installed");
            getLogger().warning("Certain features of this plugin may not function correctly");
        }
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

        } catch (IOException | InvalidConfigurationException e) {
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
        } catch (IOException | InvalidConfigurationException ex) {
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
            Iterator<FBPlayer> iterator = FBPlayer.getFBPlayers().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                iterator.remove();
            }

            String line;

            while ((line = reader.readLine()) != null) {
                new FBPlayer(line);
            }

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (FBPlayer.get(uuid) == null) {
                    new FBPlayer(player.getUniqueId().toString() + "," + player.getName() + ",0");
                }
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void saveDatabase() {
        try (BufferedWriter writer = Files.newBufferedWriter(this.path, ENCODING)) {
            for (FBPlayer fbplayer : FBPlayer.getFBPlayers()) {
                Iterator<CommandEditor> editors = fbplayer.getCooldowns().keySet().iterator();
                CommandEditor editor;
                while (editors.hasNext()) {
                    editor = editors.next();
                    int difference = (int) (System.currentTimeMillis() - fbplayer.getCooldown(editor)) / 1000;

                    if (difference > editor.getCooldown()) {
                        fbplayer.removeCooldown(editor);
                    }
                }

                if (!fbplayer.getCooldowns().isEmpty() || fbplayer.getCrates() != 0) {
                    writer.write(fbplayer.toString());
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}