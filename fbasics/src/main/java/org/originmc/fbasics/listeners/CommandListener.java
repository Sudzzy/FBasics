package org.originmc.fbasics.listeners;

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
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.CommandEditor;
import org.originmc.fbasics.task.WarmupTask;

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
    private final Map<UUID, WarmupTask> warmups = new HashMap<>();

    public CommandListener(FBasics plugin) {
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

        if (config.getBoolean("commands.enabled")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Commands module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPlayerCommandLowest(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().matches(this.priority)) {
            handleCommand(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerCommandHighest(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().matches(this.priority)) {
            handleCommand(event);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!this.warmups.containsKey(uuid)) {
            return;
        }

        Location from = event.getFrom();
        Location to = event.getTo();

        int x = Math.abs((int) from.getX() - (int) to.getX());
        int y = Math.abs((int) from.getY() - (int) to.getY());
        int z = Math.abs((int) from.getZ() - (int) to.getZ());

        if (!(x >= 1 || y >= 1 || z >= 1)) {
            return;
        }

        removeWarmup(uuid);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if (this.warmups.containsKey(uuid)) {
            removeWarmup(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (this.warmups.containsKey(uuid)) {
            removeWarmup(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }

    public void handleCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        CommandEditor commandEditor;

        if (this.ignoreCase) {
            commandEditor = CommandEditor.getCommandEditor(command.toLowerCase());
        } else {
            commandEditor = CommandEditor.getCommandEditor(command);
        }

        if (commandEditor == null) {
            return;
        }

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
        }

        if (commandEditor.getPerm() != null && !player.hasPermission(commandEditor.getPerm())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
            event.setCancelled(true);
            return;
        }

        if (!commandEditor.getBlocks().isEmpty() && !player.hasPermission(PERMISSION_BLOCKS)) {

            Location location = player.getLocation();
            Material block1 = location.getBlock().getType();
            Material block2 = location.getBlock().getRelative(0, 1, 0).getType();

            if (!commandEditor.getBlocks().contains(block1) || !commandEditor.getBlocks().contains(block2)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageBlock));
                event.setCancelled(true);
                return;
            }
        }

        if (this.plugin.getFactionsHook() != null &&
                this.plugin.getFactionsHook().isInTerritory(player, commandEditor.getFactions())) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageFaction));
            event.setCancelled(true);
            return;
        }

        if (commandEditor.getCooldown() > 0 && !player.hasPermission(PERMISSION_COOLDOWN)) {
            long remaining = commandEditor.getCooldown() -
                    (System.currentTimeMillis() - commandEditor.getActiveCooldown(uuid)) / 1000L;

            if (remaining > 0L) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCooldown.replace("{COOLDOWN}", String.valueOf(remaining))));
                event.setCancelled(true);
                return;
            }

            commandEditor.setActiveCooldown(uuid, System.currentTimeMillis());
        }

        if (!player.hasPermission(PERMISSION_WARMUP) && commandEditor.getWarmup() > 0) {
            if (this.warmups.containsKey(uuid)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageWarmupDouble));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageWarmup.replace("{WARMUP}", "" + commandEditor.getWarmup())));
                this.warmups.put(uuid, new WarmupTask(this.plugin, this, player, command.substring(1), commandEditor.getPrice(), commandEditor.getWarmup()));
            }

            event.setCancelled(true);
            return;
        }

        if (!billPlayer(player, commandEditor.getPrice())) {
            event.setCancelled(true);
            return;
        }

        event.setMessage(command);
    }

    public boolean billPlayer(Player player, double price) {
        if (this.plugin.getEconomy() != null && !player.hasPermission(PERMISSION_ECONOMY) && price != 0) {
            double balance = this.plugin.getEconomy().getBalance(player);

            if (balance < price) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageInvalidFunds));
                return false;
            }

            this.plugin.getEconomy().withdrawPlayer(player, price);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePaid.replace("{MONEY}", String.valueOf(price))));
        }
        return true;
    }

    public void removeWarmup(UUID uuid) {
        this.warmups.remove(uuid).stopTask();
    }
}
