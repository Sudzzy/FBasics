package org.originmc.fbasics.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.DatabaseManager;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.Crate;
import org.originmc.fbasics.task.SetupDatabaseTask;
import org.originmc.fbasics.task.UpdateDatabaseTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CmdCrate implements CommandExecutor {

    private static final String PERMISSION_BALANCE = "fbasics.commands.crate.balance";
    private static final String PERMISSION_BALANCE_OTHER = "fbasics.commands.crate.balance.other";
    private static final String PERMISSION_CHANGE = "fbasics.commands.crate.change";
    private static final String PERMISSION_OPEN = "fbasics.commands.crate.open";
    private static final String PERMISSION_PAY = "fbasics.commands.crate.pay";
    private final boolean newAlgorithm;
    private final FBasics plugin;
    private final String messageBalance;
    private final String messageBalanceOther;
    private final String messageChanged;
    private final String messagePaymentSent;
    private final String messagePaymentReceived;
    private final String messageNotEnough;
    private final String messageInvalid;
    private final String messageConsole;
    private final String messageInvalidPlayer;
    private final String messagePermission;
    private final List<String> messageHelp;
    private final Map<String, Crate> crates = new HashMap<>();

    public CmdCrate(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.newAlgorithm = config.getBoolean("crates.new-reward-algorithm");
        this.messageNotEnough = error + language.getString("crates.error.balance");
        this.messageInvalid = error + language.getString("crates.error.invalid");
        this.messageBalance = info + language.getString("crates.info.balance");
        this.messageBalanceOther = info + language.getString("crates.info.balance-other");
        this.messageChanged = info + language.getString("crates.info.changed");
        this.messagePaymentSent = info + language.getString("crates.info.payment-sent");
        this.messagePaymentReceived = info + language.getString("crates.info.payment-received");
        this.messageConsole = error + language.getString("general.error.console");
        this.messageInvalidPlayer = error + language.getString("general.error.player");
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageHelp = language.getStringList("general.help");

        for (String crate : config.getConfigurationSection("crates.rewards").getKeys(false)) {
            this.crates.put(crate, new Crate(config, crate));
        }

        if (config.getBoolean("crates.enabled")) {
            plugin.getCommand("crate").setExecutor(this);
            new SetupDatabaseTask(plugin).runTaskAsynchronously(plugin);
            new UpdateDatabaseTask(plugin).runTaskTimerAsynchronously(plugin, 6000, 6000);
        }
    }


    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        DatabaseManager database = new DatabaseManager(this.plugin);
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageConsole));
                return true;
            }

            Player player = (Player) sender;
            args[0] = args[0].toLowerCase();

            if (args[0].matches("bal|balance")) {
                if (!player.hasPermission(PERMISSION_BALANCE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }
                String name = player.getName().toLowerCase();
                int crates = database.getCrates(name);
                String msg = this.messageBalance.replace("{CRATES}", "" + crates);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            /* Open */
            if (args[0].matches("open")) {
                if (!player.hasPermission(PERMISSION_OPEN)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                String name = player.getName().toLowerCase();
                int crates = database.getCrates(name);

                if (crates <= 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageNotEnough));
                    return true;
                }

                if (this.newAlgorithm) {
                    newAlgorithm(player);
                } else {
                    oldAlgorithm(player);
                }

                int removed = crates - 1;
                database.setCrates(name, removed);
                return true;
            }
        }

        if (args.length == 2) {

            /* Balance (Other) */
            if ((args[0].toLowerCase().matches("bal|balance"))) {
                if (!sender.hasPermission(PERMISSION_BALANCE_OTHER)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                String name = args[1];
                int crates = database.getCrates(name);

                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageBalanceOther.replace("{NAME}", name).replace("{CRATES}", "" + crates)));
                return true;
            }
        }

        if (args.length == 3) {
            args[0] = args[0].toLowerCase();

            /* Set */
            if (args[0].matches("set")) {
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                String name = args[1].toLowerCase();
                int crates = getValidInteger(args[2]);
                database.setCrates(name, crates);
                String msg = this.messageChanged.replace("{NAME}", name).replace("{CRATES}", "" + crates);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            /* Add */
            if (args[0].matches("add|give")) {
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                String name = args[1].toLowerCase();
                int crates = getValidInteger(args[2]);
                int added = database.getCrates(name) + crates;
                database.setCrates(name, added);
                String msg = this.messageChanged.replace("{NAME}", name).replace("{CRATES}", "" + added);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            /* Remove */
            if (args[0].matches("remove|rem")) {
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                String name = args[1].toLowerCase();
                int crates = getValidInteger(args[2]);
                int removed = database.getCrates(name) - crates;
                database.setCrates(name, removed);
                String msg = this.messageChanged.replace("{NAME}", name).replace("{CRATES}", "" + removed);
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                return true;
            }

            /* Pay */
            if (args[0].matches("pay")) {
                if (!sender.hasPermission(PERMISSION_PAY)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messagePermission));
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageConsole));
                    return true;
                }

                @SuppressWarnings("deprecation")
                Player receiver = Bukkit.getPlayer(args[1]);
                Player player = (Player) sender;
                String playerName = player.getName().toLowerCase();
                String receiverName = args[1].toLowerCase();
                int crates = getValidInteger(args[2]);
                int playerCrates = database.getCrates(playerName);

                if (receiver == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageInvalidPlayer));
                    return true;
                }

                if (crates <= 0) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageInvalid));
                    return true;
                }

                if (playerCrates < crates) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageNotEnough));
                    return true;
                }

                playerCrates = playerCrates - crates;
                database.setCrates(playerName, playerCrates);
                int receiverCrates = crates + database.getCrates(receiverName);
                database.setCrates(receiverName, receiverCrates);
                String playerMsg = this.messagePaymentSent.replace("{CRATES}", String.valueOf(crates)).replace("{NAME}", receiver.getName());
                String receiverMsg = this.messagePaymentReceived.replace("{CRATES}", String.valueOf(crates)).replace("{NAME}", player.getName());
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', playerMsg));
                receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', receiverMsg));
                return true;
            }
        }

        for (String msg : this.messageHelp) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return false;
    }

    private void newAlgorithm(Player player) {
        String name = player.getName().toLowerCase();
        Random random = new Random();

        for (String reward : this.crates.keySet()) {
            double chance = 1.0D / Double.parseDouble(reward);

            if (chance < random.nextDouble()) {
                continue;
            }

            String message = this.crates.get(reward).getMessage();
            List<String> commands = this.crates.get(reward).getCommands();

            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
            }

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
        }
    }

    private void oldAlgorithm(Player player) {
        String name = player.getName().toLowerCase();
        Random random = new Random();
        int rewards = this.crates.size();
        int reward = random.nextInt(rewards);
        String path = String.valueOf(this.crates.keySet().toArray()[reward]);
        String message = this.crates.get(path).getMessage();
        List<String> commands = this.crates.get(path).getCommands();

        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
    }

    private int getValidInteger(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
