package org.originmc.fbasics.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
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
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.CommandEditor;
import org.originmc.fbasics.task.WarmupTask;

import java.util.*;

public class CommandListener implements Listener {

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
    private final String permissionBlocks = "fbasics.bypass.commands.blocks";
    private final String permissionCooldown = "fbasics.bypass.commands.cooldowns";
    private final String permissionEconomy = "fbasics.bypass.commands.economy";
    private final String permissionTerritory = "fbasics.bypass.commands.territory";
    private final String permissionWarmup = "fbasics.bypass.commands.warmup";
    private final List<CommandEditor> editors = new ArrayList<CommandEditor>();
    private Map<UUID, WarmupTask> warmups = new HashMap<UUID, WarmupTask>();

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

        for (String editor : config.getConfigurationSection("commands.editors").getKeys(false))
            this.editors.add(new CommandEditor(config, editor));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommandLowest(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        if (!command.matches(this.priority)) return;

        Player player = event.getPlayer();
        CommandEditor commandEditor;

        if (this.ignoreCase) {
            commandEditor = getCommandEditor(command.toLowerCase());
        } else {
            commandEditor = getCommandEditor(command);
        }

        if (commandEditor == null) return;

        command = getNewCommand(command, commandEditor.getAlias(), commandEditor.getRegex());

        if (!hasPermission(player, commandEditor.getPerm())) {
            event.setCancelled(true);
            return;
        }

        if (isInsideBlock(player, commandEditor.getBlocks())) {
            event.setCancelled(true);
            return;
        }

        if (isInsideClaim(player, commandEditor.getFactions())) {
            event.setCancelled(true);
            return;
        }

        if (!setCooldown(player, commandEditor)) {
            event.setCancelled(true);
            return;
        }

        if (setWarmup(player, commandEditor, command)) {
            event.setCancelled(true);
            return;
        }

        if (!billPlayer(player, commandEditor.getPrice())) {
            event.setCancelled(true);
            return;
        }

        event.setMessage(command);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onCommandHighest(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();

        if (command.matches(this.priority)) return;

        Player player = event.getPlayer();
        CommandEditor commandEditor;

        if (this.ignoreCase) {
            commandEditor = getCommandEditor(command.toLowerCase());
        } else {
            commandEditor = getCommandEditor(command);
        }

        if (commandEditor == null) return;

        command = getNewCommand(command, commandEditor.getAlias(), commandEditor.getRegex());

        if (!hasPermission(player, commandEditor.getPerm())) {
            event.setCancelled(true);
            return;
        }

        if (isInsideBlock(player, commandEditor.getBlocks())) {
            event.setCancelled(true);
            return;
        }

        if (isInsideClaim(player, commandEditor.getFactions())) {
            event.setCancelled(true);
            return;
        }

        if (!setCooldown(player, commandEditor)) {
            event.setCancelled(true);
            return;
        }

        if (setWarmup(player, commandEditor, command)) {
            event.setCancelled(true);
            return;
        }

        if (!billPlayer(player, commandEditor.getPrice())) {
            event.setCancelled(true);
            return;
        }

        event.setMessage(command);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!this.warmups.containsKey(uuid)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        int x = Math.abs((int) from.getX() - (int) to.getX());
        int y = Math.abs((int) from.getY() - (int) to.getY());
        int z = Math.abs((int) from.getZ() - (int) to.getZ());

        if (!(x >= 1 || y >= 1 || z >= 1)) return;

        this.removeWarmup(uuid);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if (this.warmups.containsKey(uuid)) {
            this.removeWarmup(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (this.warmups.containsKey(uuid)) {
            this.removeWarmup(uuid);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCancelled));
        }
    }

    public void removeWarmup(UUID uuid) {
        this.warmups.remove(uuid).stopTask();
    }

    private String getNewCommand(String command, String alias, String matcher) {
        if (alias == null) return command;

        String allArgs = "";
        String[] args = command.split(" ");
        int matcherLength = matcher.replace(" .*", "").replace(".*", "").split(" ").length;
        int allArgsLength = args.length - matcherLength;

        for (int c = 0; c < allArgsLength; c++)
            allArgs = allArgs + " " + args[c + matcherLength];

        command = alias.replace("{ALL_ARGS}", allArgs);
        args[0] = args[0].substring(1);

        for (int c = 0; c < args.length; c++)
            command = command.replace("{ARG:" + c + "}", args[c]);

        return command;
    }

    private boolean hasPermission(Player player, String permission) {
        if (permission != null && !player.hasPermission(permission)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
            return false;
        }
        return true;
    }

    private boolean isInsideBlock(Player player, List<Material> blocks) {
        if (!blocks.isEmpty() && !player.hasPermission(this.permissionBlocks)) {

            Location location = player.getLocation();
            Material block1 = location.getBlock().getType();
            Material block2 = location.getBlock().getRelative(0, 1, 0).getType();

            if (!blocks.contains(block1) || !blocks.contains(block2)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageBlock));
                return true;
            }
        }
        return false;
    }

    private boolean isInsideClaim(Player player, List<String> factions) {
        if (factions.isEmpty() || Bukkit.getPluginManager().getPlugin("Factions") == null || player.hasPermission(this.permissionTerritory))
            return false;

        Location location = player.getLocation();
        String factionsVersion = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();

        if (factionsVersion.startsWith("1")) {

            FLocation flocation = new FLocation(location);
            com.massivecraft.factions.Faction faction1 = Board.getFactionAt(flocation);
            com.massivecraft.factions.Faction faction2 = FPlayers.i.get(player).getFaction();

            for (String f : factions) {
                if ((f.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) || f.equalsIgnoreCase(faction1.getTag()) || f.equalsIgnoreCase(faction1.getTag().substring(2))) {
                    return false;
                }
            }
        } else if (factionsVersion.startsWith("2.6.0")) {

            com.massivecraft.factions.entity.Faction faction1 = BoardColls.get().getFactionAt(PS.valueOf(location));
            com.massivecraft.factions.entity.Faction faction2 = UPlayer.get(player).getFaction();

            for (String f : factions) {
                if ((f.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) || f.equalsIgnoreCase(faction1.getName()) || f.equalsIgnoreCase(faction1.getName().substring(2))) {
                    return false;
                }
            }
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageFaction));
        return true;
    }

    private CommandEditor getCommandEditor(String command) {
        for (CommandEditor commandEditor : this.editors) {
            if (command.matches(commandEditor.getRegex())) return commandEditor;
        }
        return null;
    }

    private boolean setCooldown(Player player, CommandEditor commandEditor) {
        UUID uuid = player.getUniqueId();
        int cooldown = commandEditor.getCooldown();

        if (cooldown > 0 && !player.hasPermission(this.permissionCooldown)) {
            long remaining = cooldown - (System.currentTimeMillis() - commandEditor.getActiveCooldown(uuid)) / 1000L;

            if (remaining > 0L) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCooldown.replace("{COOLDOWN}", String.valueOf(remaining))));
                return false;
            }

            commandEditor.setActiveCooldown(uuid, System.currentTimeMillis());
        }
        return true;
    }

    public boolean billPlayer(Player player, double price) {
        if (this.plugin.getEconomy() != null && !player.hasPermission(this.permissionEconomy) && price != 0) {
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

    private boolean setWarmup(Player player, CommandEditor commandEditor, String command) {
        if (player.hasPermission(this.permissionWarmup)) return false;

        UUID uuid = player.getUniqueId();
        int warmup = commandEditor.getWarmup();

        if (warmup > 0) {

            if (this.warmups.containsKey(uuid)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageWarmupDouble));
                return true;
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageWarmup.replace("{WARMUP}", "" + warmup)));
            this.warmups.put(uuid, new WarmupTask(this.plugin, this, player, command.substring(1), commandEditor.getPrice(), warmup));
            return true;
        }

        return false;
    }
}
