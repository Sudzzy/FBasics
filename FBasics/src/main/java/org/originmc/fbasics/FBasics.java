package org.originmc.fbasics;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.originmc.fbasics.command.CommandType;
import org.originmc.fbasics.entity.CommandModifier;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.factions.api.FactionsHook;
import org.originmc.fbasics.factions.api.IFactionsHook;
import org.originmc.fbasics.listener.AntiPhaseListener;
import org.originmc.fbasics.listener.AntilooterListener;
import org.originmc.fbasics.listener.BookLimitListener;
import org.originmc.fbasics.listener.CommandListener;
import org.originmc.fbasics.listener.CropDupeListener;
import org.originmc.fbasics.listener.DismountListener;
import org.originmc.fbasics.listener.DispenserListener;
import org.originmc.fbasics.listener.EnderpearlListener;
import org.originmc.fbasics.listener.FactionMapListener;
import org.originmc.fbasics.listener.InventoryDupeListener;
import org.originmc.fbasics.listener.McMMOMiningListener;
import org.originmc.fbasics.listener.NetherRoofListener;
import org.originmc.fbasics.placeholder.EnderpearlCooldownPlaceholder;
import org.originmc.fbasics.settings.Settings;
import org.originmc.fbasics.util.SettingsUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static org.originmc.fbasics.util.DurationUtils.calculateRemaining;

public final class FBasics extends JavaPlugin {

    private final File database = new File(getDataFolder() + File.separator + "userdata.json");

    private Economy economy = null;

    private Permission permissions = null;

    private IFactionsHook factions;

    private Settings settings;

    private HashMap<UUID, User> users = new HashMap<>();

    public Permission getPermissions() {
        return permissions;
    }

    public Economy getEconomy() {
        return economy;
    }

    public IFactionsHook getFactions() {
        return factions;
    }

    public Settings getSettings() {
        return settings;
    }

    public HashMap<UUID, User> getUsers() {
        return users;
    }

    public User getOrCreateUser(UUID playerId) {
        User user = users.get(playerId);
        if (user == null) {
            user = new User();
            users.put(playerId, user);
        }
        return user;
    }

    @Override
    public void onEnable() {
        // Get an updated version of the configuration.
        update();

        // Load all the configuration into settings system.
        settings = new Settings(this);
        settings.load();

        // Load the user database.
        loadDatabase();

        // Load dependencies.
        integrateFactions();
        integratePlaceholders();
        integrateVault();

        // Create and register all listeners.
        new AntilooterListener(this);
        new AntiPhaseListener(this);
        new FactionMapListener(this);
        new BookLimitListener(this);
        new InventoryDupeListener(this);
        new CommandListener(this);
        new CropDupeListener(this);
        new DismountListener(this);
        new DispenserListener(this);
        new EnderpearlListener(this);
        new McMMOMiningListener(this);
        new NetherRoofListener(this);
    }

    @Override
    public void onDisable() {
        cleanDatabase();
        saveDatabase();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
        return CommandType.fromCommand(this, sender, args).execute();
    }

    /**
     * Loads Vault dependency.
     */
    public void integrateVault() {
        // Do nothing if Vault is not enabled.
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("*** WARNING ***");
            getLogger().warning("Vault is not installed!");
            getLogger().warning("Economy and permissions support is disabled.");
            return;
        }

        // Grab Economy support.
        RegisteredServiceProvider<Economy> economy = Bukkit.getServicesManager().getRegistration(Economy.class);
        this.economy = economy.getProvider();

        // Grab Permissions support.
        RegisteredServiceProvider<Permission> permissions = Bukkit.getServicesManager().getRegistration(Permission.class);
        this.permissions = permissions.getProvider();
    }

    /**
     * Loads MVdW placeholders API dependency.
     */
    public void integratePlaceholders() {
        // Create and register new placeholders.
        Plugin plugin = Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI");
        if (plugin instanceof PlaceholderAPI) {
            new EnderpearlCooldownPlaceholder(this);
        }
    }

    /**
     * Creates a factions hook depending on which factions version the server is running.
     */
    public void integrateFactions() {
        String version = null;
        switch (settings.getFactionsVersion()) {
            case AUTO:
                // Do nothing if factions is not enabled.
                Plugin plugin = Bukkit.getPluginManager().getPlugin("Factions");
                if (plugin == null || !plugin.isEnabled()) break;

                String[] v = plugin.getDescription().getVersion().split("\\.");
                version = v[0] + "_" + v[1];

                // Special case for HCF - Use FactionsUUID 1.6 hook.
                // Special case for 2.8 - Use Factions 2.7 hook.
                if (version.compareTo("1_6") < 0) {
                    version = "1_6";
                } else if (version.compareTo("2_7") > 0) {
                    version = "2_7";
                }
                break;

            case V1_6:
            case V1_8:
            case V2_6:
            case V2_7:
                version = settings.getFactionsVersion().name().toLowerCase();
                break;
        }

        // If the factions version is invalid, drop all factions support.
        if (version == null) {
            factions = new FactionsHook();
            getLogger().info("Not using any Factions support.");
            return;
        }

        // Determine which factions helper implementation to use.
        String className = "org.originmc.fbasics.factions.v" + version + ".FactionsHook";

        try {
            // Create and add the IFactionsHook.
            factions = (IFactionsHook) Class.forName(className).newInstance();
            getLogger().info("Using Factions v" + version + " support.");
        } catch (Exception e) {
            // Something went wrong, chances are it's a newer, incompatible Factions.
            getLogger().warning("**WARNING**");
            getLogger().warning("Failed to enable Factions integration due to errors.");
            getLogger().warning("This is most likely due to an unsupported version of Factions.");
            getLogger().warning("Defaulting to a non-factions configuration.");

            // Set the factions hook to default.
            factions = new FactionsHook();

            // Let's leave a stack trace in console for reporting.
            e.printStackTrace();
        }
    }

    /**
     * Updates the configuration file for this plugin.
     */
    public void update() {
        // Save the configuration file if there is currently not one available.
        saveDefaultConfig();

        // Do nothing if the configuration version is the same as latest.
        if (getConfig().getInt("config-version", 0) == getConfig().getDefaults().getInt("config-version", 0)) return;

        // Do nothing if the config is set to not update.
        if (!getConfig().getBoolean("update-config", true)) {
            getLogger().warning("*** WARNING ***");
            getLogger().warning("Your configuration file is outdated, please update it!");
            return;
        }

        // Attempt to clean FBasics' folder for the new configuration.
        moveUnusedFiles();

        // Save the updated configuration file.
        saveDefaultConfig();
    }

    private void moveUnusedFiles() {
        // Do nothing if directory listing is null.
        File dir = new File(getDataFolder() + File.separator);
        File[] directoryListing = dir.listFiles();
        if (directoryListing == null) return;

        // Move all unused files into the "old_files" folder.
        dir = new File(getDataFolder() + File.separator + "old_files" + File.separator);
        dir.mkdirs();
        for (File child : directoryListing) {
            child.renameTo(new File(dir.getAbsolutePath() + File.separator + child.getName()));
        }
    }

    /**
     * Saves the current user database to disk.
     */
    public void saveDatabase() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        String userdata = gson.toJson(users);

        try {
            // Initialize the parent directory and file.
            SettingsUtils.initialize(database);

            // Write all lines to the database.
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(database));
            writer.write(userdata);
            writer.close();
        } catch (IOException e) {
            getLogger().severe("An error occurred when attempting to save the user database!");
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads the current user database from disk.
     */
    public void loadDatabase() {
        StringBuilder builder = new StringBuilder();

        try {
            // Do nothing if files were not already created.
            SettingsUtils.initialize(database);

            // Read all lines from the database.
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(database)));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            getLogger().severe("An error occurred when attempting to load the user database!");
            throw new RuntimeException(e);
        }

        String userdata = builder.toString();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        HashMap<UUID, User> users = gson.fromJson(userdata, new TypeToken<Map<UUID, User>>() {
        }.getType());
        if (users != null) {
            this.users = users;
        }
    }

    /**
     * Cleans the plugins' database of any outdated information.
     */
    public void cleanDatabase() {
        Iterator<User> users = this.users.values().iterator();
        User user;
        while (users.hasNext()) {
            user = users.next();
            if (cleanUser(user)) {
                users.remove();
            }
        }
    }

    /**
     * Cleans any outdated information from the user and checks to see if the user should still be stored within the
     * main database.
     *
     * @param user the user to clean and check.
     * @return true if the user should removed from the database.
     */
    private boolean cleanUser(User user) {
        Iterator<CommandModifier> modifiers = user.getModifiers().values().iterator();
        CommandModifier modifier;

        while (modifiers.hasNext()) {
            modifier = modifiers.next();
            if (cleanModifier(modifier)) {
                modifiers.remove();
            }
        }

        return calculateRemaining(user.getLooterMessageCooldown()) <= 0 &&
                calculateRemaining(user.getEnderpearlCooldown()) <= 0 &&
                calculateRemaining(user.getEnderpearlDoorCooldown()) <= 0 &&
                user.getModifiers().isEmpty() &&
                user.getWildernessTask() == null;
    }

    /**
     * Checks to see if the command modifier still has any active cooldowns, warmups or tasks.
     *
     * @param modifier the modifier to check for.
     * @return true if the modifier should be removed from the database.
     */
    private boolean cleanModifier(CommandModifier modifier) {
        return calculateRemaining(modifier.getCooldown()) <= 0 &&
                calculateRemaining(modifier.getWarmup()) <= 0 &&
                modifier.getTask() == null;
    }

}
