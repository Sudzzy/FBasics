package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.CommandEditor;
import org.originmc.fbasics.FBPlayer;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.task.WarmupTask;
import org.originmc.fbasics.util.DurationUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommandListener implements Listener {

    private static final String PERMISSION_BLOCKS = "fbasics.bypass.commands.blocks";

    private static final String PERMISSION_COOLDOWN = "fbasics.bypass.commands.cooldowns";

    private static final String PERMISSION_ECONOMY = "fbasics.bypass.commands.economy";

    private static final String PERMISSION_WARMUP = "fbasics.bypass.commands.warmup";

    private final boolean ignoreCase;

    private final FBasics plugin;

    private final String priority;

    private final String messageBlock;

    private final String messageCancelled;

    private final String messageCooldown;

    private final String messageFaction;

    private final String messageInvalidFunds;

    private final String messagePaid;

    private final String messagePermission;

    private final String messageWarmup;

    private final String messageWarmupDouble;

    private final Map<UUID, WarmupTask> warmupTasks = new HashMap<>();

    public CommandListener(FBasics plugin) {
        // Load all settings for the Commands module
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.priority = config.getString("commands.priority");
        this.ignoreCase = config.getBoolean("commands.ignore-case");
        this.messageBlock = error + language.getString("commands.error.block");
        this.messageCancelled = info + language.getString("commands.info.cancelled");
        this.messageCooldown = info + language.getString("commands.info.cooldown");
        this.messageFaction = error + language.getString("commands..error.faction");
        this.messageInvalidFunds = error + language.getString("commands.error.funds");
        this.messagePaid = info + language.getString("commands.info.paid");
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageWarmup = info + language.getString("commands.info.warmup");
        this.messageWarmupDouble = error + language.getString("commands.error.warmup");

        for (String editor : config.getConfigurationSection("commands.editors").getKeys(false)) {
            new CommandEditor(config, editor);
        }

        // Register Commands events to the server if stated in the config
        if (config.getBoolean("commands.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Commands module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerCommandLowest(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().matches(priority)) {
            handleCommand(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerCommandHighest(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().matches(priority)) {
            handleCommand(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(PlayerMoveEvent event) {
        // Do nothing if player does not have a warmup
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Do nothing if player has not moved a whole block
        Location t = event.getTo();
        Location f = event.getFrom();
        if (!(t.getBlockX() != f.getBlockX() ||
                t.getBlockY() != f.getBlockY() ||
                t.getBlockZ() != f.getBlockZ())) {
            return;
        }

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(EntityDamageEvent event) {
        // Do nothing if entity is not a player
        if (!(event.getEntity() instanceof Player)) return;

        // Do nothing if player does not have a warmup
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmup(PlayerTeleportEvent event) {
        // Do nothing if player does not have a warmup
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!warmupTasks.containsKey(uuid)) return;

        // Remove the players warmup
        removeWarmup(uuid);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCancelled));
    }

    public void handleCommand(PlayerCommandPreprocessEvent event) {
        // Attempt to find the CommandEditor for this command
        String command = event.getMessage();
        CommandEditor commandEditor;
        if (ignoreCase) {
            commandEditor = CommandEditor.getByCommand(command.toLowerCase());
        } else {
            commandEditor = CommandEditor.getByCommand(command);
        }

        // Do nothing if the CommandEditor is null
        if (commandEditor == null) return;

        // Attempt to alias the command
        if (commandEditor.getAlias() != null) {
            String allArgs = "";
            String[] args = command.split(" ");
            int matcherLength = commandEditor.getRegex().replace(" .*", "").replace(".*", "").split(" ").length;
            int allArgsLength = args.length - matcherLength;

            for (int c = 0; c < allArgsLength; c++) {
                allArgs = allArgs + " " + args[c + matcherLength];
            }

            command = commandEditor.getAlias().replace("{ALL_ARGS}", allArgs);
            args[0] = args[0].substring(1);

            for (int c = 0; c < args.length; c++) {
                command = command.replace("{ARG:" + c + "}", args[c]);
            }
            event.setMessage(command);
        }

        // Deny the command if player does not have permission
        Player player = event.getPlayer();
        if (commandEditor.getPerm() != null && !player.hasPermission(commandEditor.getPerm())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
            event.setCancelled(true);
            return;
        }

        // Check if the command has whitelisted blocks and if player has permission
        if (!commandEditor.getBlocks().isEmpty() && !player.hasPermission(PERMISSION_BLOCKS)) {
            // Deny the command if player is not within any of the listed block types
            Location location = player.getLocation();
            Material block1 = location.getBlock().getType();
            Material block2 = location.getBlock().getRelative(0, 1, 0).getType();
            if (!commandEditor.getBlocks().contains(block1) || !commandEditor.getBlocks().contains(block2)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageBlock));
                event.setCancelled(true);
                return;
            }
        }

        // Deny the command if player is within the specified territories
        if (plugin.getFactionsManager().isInTerritory(player, commandEditor.getFactions())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageFaction));
            event.setCancelled(true);
            return;
        }

        // Check if the command has a cooldown and if player has permission
        UUID uuid = player.getUniqueId();
        if (commandEditor.getCooldown() > 0 && !player.hasPermission(PERMISSION_COOLDOWN)) {
            FBPlayer fbplayer = FBPlayer.get(uuid);

            // Do nothing if the player has no data to prevent errors
            if (fbplayer == null) return;


            // Deny the command if player is on a cooldown
            long remaining = commandEditor.getCooldown() -
                    (System.currentTimeMillis() - fbplayer.getCooldown(commandEditor)) / 1000L;

            if (remaining > 0L) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCooldown
                        .replace("{COOLDOWN}", DurationUtils.format(remaining))));

                event.setCancelled(true);
                return;
            }

            // Give the player a cooldown for this command
            fbplayer.setCooldown(commandEditor);
        }

        // Check if the command has a warmup and if player has permission
        if (!player.hasPermission(PERMISSION_WARMUP) && commandEditor.getWarmup() > 0) {
            if (warmupTasks.containsKey(uuid)) {
                // Send player a message as they are already on a warmup
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageWarmupDouble));
            } else {
                // Send player a message that the warmup is starting
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageWarmup
                        .replace("{WARMUP}", DurationUtils.format(commandEditor.getWarmup()))));

                // Start the warmup for the player
                warmupTasks.put(uuid, new WarmupTask(plugin, this, player, command.substring(1),
                        commandEditor.getPrice(), commandEditor.getWarmup()));
            }

            event.setMessage("/fbasics null");
            event.setCancelled(true);
            return;
        }

        // Bill the player if the command has a price
        if (!billPlayer(player, commandEditor.getPrice())) {
            event.setCancelled(true);
        }
    }

    public boolean billPlayer(Player player, double price) {
        // Do nothing if economy is not enabled
        if (plugin.getEconomy() == null) return true;

        // Do nothing if player has permission
        if (player.hasPermission(PERMISSION_ECONOMY)) return true;

        // Do nothing if command is free
        if (price == 0) return true;

        // Deny command if player does not have enough money
        double balance = plugin.getEconomy().getBalance(player);
        if (balance < price) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageInvalidFunds
                    .replace("{PRICE}", String.valueOf(price))));
            return false;
        }

        // Withdraw money from player
        plugin.getEconomy().withdrawPlayer(player, price);

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePaid
                .replace("{PRICE}", String.valueOf(price))));

        return true;
    }

    public void removeWarmup(UUID uuid) {
        warmupTasks.remove(uuid).stopTask();
    }

}