package org.originmc.fbasics.listener;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Perm;
import org.originmc.fbasics.entity.CommandModifier;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.event.CommandModifierEvent;
import org.originmc.fbasics.settings.CommandModifierGroupSettings;
import org.originmc.fbasics.settings.CommandModifierSettings;
import org.originmc.fbasics.settings.CommandSettings;
import org.originmc.fbasics.task.CommandWarmupTask;
import org.originmc.fbasics.util.DurationUtils;
import org.originmc.fbasics.util.MessageUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommandListener implements Listener {

    private final FBasics plugin;

    private final CommandSettings settings;

    public CommandListener(FBasics plugin) {
        this.plugin = plugin;
        settings = plugin.getSettings().getCommandSettings();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void lowest(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.LOWEST);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void low(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.LOW);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void normal(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.NORMAL);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void high(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.HIGH);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void highest(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.HIGHEST);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void monitor(PlayerCommandPreprocessEvent event) {
        execute(event, EventPriority.MONITOR);
    }

    private void execute(PlayerCommandPreprocessEvent event, EventPriority priority) {
        // Do nothing if this module is not enabled.
        if (!settings.isEnabled()) return;

        // Do nothing if there are no modifiers for this event.
        Set<CommandModifierSettings> modifiers = settings.getModifiers().get(priority);
        if (modifiers == null) return;

        // Do nothing if there are no modifier settings that match this command.
        CommandModifierSettings settings = getModifier(event.getMessage().toLowerCase(), modifiers);
        if (settings == null) return;

        // Attempt to load the users' group specific modifier settings.
        Player player = event.getPlayer();
        CommandModifierGroupSettings groupSettings = settings;
        if (plugin.getPermissions() != null && plugin.getPermissions().hasGroupSupport()) {
            String group = plugin.getPermissions().getPrimaryGroup(player);
            if (settings.getGroups().containsKey(group)) {
                groupSettings = settings.getGroups().get(group);
            }
        }

        // Get this players' plugin user profile.
        User user = plugin.getOrCreateUser(player.getUniqueId());

        // Attempt to get or create a command modifier under this users' profile with this command modifiers' name.
        CommandModifier modifier = user.getModifiers().get(settings.getName());
        if (modifier == null) {
            modifier = new CommandModifier(settings.getName());
            user.getModifiers().put(settings.getName(), modifier);
        }

        // Create then execute the command modifier event.
        CommandModifierEvent modifierEvent = new CommandModifierEvent(player, groupSettings, modifier, event.getMessage());
        Bukkit.getPluginManager().callEvent(modifierEvent);

        // Replace the message with what message returned.
        event.setMessage(modifierEvent.getCommand());

        // Cancel event if modifier event was cancelled.
        if (modifierEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    private CommandModifierSettings getModifier(String command, Set<CommandModifierSettings> modifiers) {
        for (CommandModifierSettings modifier : modifiers) {
            if (modifier.getRegex().matcher(command).matches()) {
                return modifier;
            }
        }
        return null;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void checkPermissions(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        CommandModifierGroupSettings settings = event.getSettings();
        if (settings.getPermission().isEmpty() || player.hasPermission(settings.getPermission())) return;

        // Send the player the permission message and cancel event.
        MessageUtils.sendMessage(player, settings.getPermissionMessage());
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void checkFactions(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.Commands.FACTIONS)) return;

        // Do nothing if player has access.
        CommandModifierGroupSettings settings = event.getSettings();
        if (plugin.getFactions().hasAccess(player, settings.getFactions(), settings.getFactionsMode())) return;

        // Send the player the factions message and cancel event.
        MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getFactionMessage()
                .replace("{command}", event.getCommand()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void checkBalance(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.Commands.PRICES)) return;

        // Do nothing if price less than or equal to nothing.
        double price = event.getSettings().getPrice();
        if (price <= 0) return;

        // Do nothing if economy is null.
        if (plugin.getEconomy() == null) return;

        // Do nothing if player has enough balance.
        if (plugin.getEconomy().has(player, price)) return;

        // Send the player the economy message and cancel event.
        MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getCannotAffordMessage()
                .replace("{amount}", "" + price));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void chargePlayer(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.Commands.PRICES)) return;

        // Do nothing if price is nothing.
        double price = event.getSettings().getPrice();
        if (price == 0) return;

        // Do nothing if no economy plugin is on the server.
        if (plugin.getEconomy() == null) return;

        // Charge the player for executing this command.
        plugin.getEconomy().withdrawPlayer(player, event.getSettings().getPrice());

        // Send player the confirmation message.
        MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getPaidMessage()
                .replace("{amount}", "" + price));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void checkCooldown(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.Commands.COOLDOWNS)) return;

        // Do nothing if cooldown is not valid.
        if (event.getSettings().getCooldown() <= 0) return;

        // Do nothing if cooldown is finished.
        long remaining = DurationUtils.calculateRemaining(event.getModifier().getCooldown());
        if (remaining <= 0) return;

        // Send the player the cooldown message and cancel event.
        MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getCooldownMessage()
                .replace("{time}", "" + DurationUtils.format(remaining))
                .replace("{command}", event.getCommand()));
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void setCooldown(CommandModifierEvent event) {
        // Do nothing if player has permission.
        if (event.getPlayer().hasPermission(Perm.Commands.COOLDOWNS)) return;

        // Do nothing if cooldown is not valid.
        if (event.getSettings().getCooldown() <= 0) return;

        // Set the cooldown.
        event.getModifier().setCooldown(System.currentTimeMillis() + event.getSettings().getCooldown());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void sendMessage(CommandModifierEvent event) {
        MessageUtils.sendMessage(event.getPlayer(), event.getSettings().getMessage());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void setCommand(CommandModifierEvent event) {
        // Do nothing if alias is empty.
        String alias = event.getSettings().getAlias();
        if (alias.isEmpty()) return;

        CommandModifierSettings root = event.getSettings().getRoot()  == null ?
                (CommandModifierSettings) event.getSettings() : event.getSettings().getRoot();
        Matcher matcher = root.getRegex().matcher(event.getCommand());
        LinkedHashMap<String, String> groupPlaceholders = new LinkedHashMap<>();
        for (int c = 0; c < matcher.groupCount(); c++) {
            if (alias.contains("$" + c)) {
                groupPlaceholders.put("$" + c, matcher.group(c));
            }
        }

        String command = alias;
        for (Map.Entry<String, String> entry : groupPlaceholders.entrySet()) {
            command = command.replace(entry.getKey(), entry.getValue());
        }

        // Load all argument placeholders.
        LinkedHashMap<String, String> placeholders = new LinkedHashMap<>();
        String[] args = event.getCommand().split(" ");
        args[0] = args[0].substring(1);
        for (int c = 0; c < args.length; c++) {
            if (alias.contains("%" + c)) {
                placeholders.put("%" + c + "+", StringUtils.join(Arrays.copyOfRange(args, c, args.length), " "));
                placeholders.put("%" + c, args[c]);
            }
        }

        // Create the new command with placeholders changed.
        for (String placeholder : placeholders.keySet()) {
            command = command.replace(placeholder, placeholders.get(placeholder));
        }

        // Set events' command to this newly generated command.
        event.setCommand(command);
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void createWarmup(CommandModifierEvent event) {
        // Do nothing if player has permission.
        Player player = event.getPlayer();
        if (player.hasPermission(Perm.Commands.WARMUPS)) return;

        // Do nothing if modifier has no warmup.
        if (event.getSettings().getWarmup() <= 0) return;

        // Check if modifier already has an active task.
        CommandModifier modifier = event.getModifier();
        CommandWarmupTask task = modifier.getTask();
        if (task != null) {
            // If the task is still running, tell the player attempting to execute a duplicate warmup is not allowed.
            // Otherwise, this is simply the warmup task being completed therefore no new warmup needs to be set.
            if (task.isRunning()) {
                MessageUtils.sendMessage(event.getPlayer(), plugin.getSettings().getCommandSettings().getWarmupDuplicateMessage());
                event.setCancelled(true);
            }

            return;
        }

        // Send the player the warmup message and start the warmup.
        MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getWarmupStartMessage()
                .replace("{time}", "" + DurationUtils.format(event.getSettings().getWarmup()))
                .replace("{command}", event.getCommand()));
        modifier.setTask(new CommandWarmupTask(plugin, event.getPlayer(), event.getSettings(), modifier, event.getCommand()));
        modifier.setWarmup(System.currentTimeMillis() + event.getSettings().getWarmup());
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmups(EntityDamageEvent event) {
        // Do nothing if not a player.
        if (!(event.getEntity() instanceof Player)) return;

        // Cancel warmups if the player does not have permission to bypass.
        Player player = (Player) event.getEntity();
        if (player.hasPermission(Perm.Commands.WARMUPS_DAMAGE)) {
            cancelWarmups(player.getUniqueId(), CancelReason.DAMAGE);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmups(PlayerMoveEvent event) {
        // Do nothing if player has permission to bypass.
        if (event.getPlayer().hasPermission(Perm.Commands.WARMUPS_MOVE)) return;

        // Cancel warmups if the player has moved a full block.
        if (hasMoved(event.getFrom(), event.getTo())) {
            cancelWarmups(event.getPlayer().getUniqueId(), CancelReason.MOVE);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void cancelWarmups(PlayerTeleportEvent event) {
        // Do nothing if player has permission to bypass.
        if (event.getPlayer().hasPermission(Perm.Commands.WARMUPS_MOVE)) return;

        // Cancel warmups if the player has moved a full block.
        if (hasMoved(event.getFrom(), event.getTo())) {
            cancelWarmups(event.getPlayer().getUniqueId(), CancelReason.MOVE);
        }
    }

    private boolean hasMoved(Location previous, Location current) {
        return previous.getBlockX() != current.getBlockX() ||
                previous.getBlockY() != current.getBlockY() ||
                previous.getBlockZ() != current.getBlockZ();
    }

    private void cancelWarmups(UUID playerId, CancelReason reason) {
        User user = plugin.getUsers().get(playerId);
        if (user == null) return;

        for (CommandModifier modifier : user.getModifiers().values()) {
            CommandWarmupTask task = modifier.getTask();
            if (task == null || !task.isRunning()) continue;

            switch (reason) {
                case DAMAGE:
                    if (!task.getSettings().isWarmupCancelOnDamage()) continue;
                    task.cancel(true);
                    break;
                case MOVE:
                    if (!task.getSettings().isWarmupCancelOnMove()) continue;
                    task.cancel(true);
                    break;
            }
        }
    }

    public enum CancelReason {

        DAMAGE, MOVE

    }

}
