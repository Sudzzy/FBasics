package org.originmc.fbasics.cmd;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.Crate;
import org.originmc.fbasics.FBPlayer;

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

    private final String messageBalance;

    private final String messageBalanceOther;

    private final String messageChanged;

    private final String messagePaymentSent;

    private final String messagePaymentReceived;

    private final String messageNotEnough;

    private final String messageOffline;

    private final String messageInvalid;

    private final String messageConsole;

    private final String messagePermission;

    private final List<String> messageHelp;

    private final Map<String, Crate> crates = new HashMap<>();

    public CmdCrate(FBasics plugin) {
        // Load all the settings for the Crates command
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.newAlgorithm = config.getBoolean("crates.new-reward-algorithm");
        this.messageNotEnough = error + language.getString("crates.error.balance");
        this.messageOffline = error + language.getString("crates.error.offline");
        this.messageInvalid = error + language.getString("crates.error.invalid");
        this.messageBalance = info + language.getString("crates.info.balance");
        this.messageBalanceOther = info + language.getString("crates.info.balance-other");
        this.messageChanged = info + language.getString("crates.info.changed");
        this.messagePaymentSent = info + language.getString("crates.info.payment-sent");
        this.messagePaymentReceived = info + language.getString("crates.info.payment-received");
        this.messageConsole = error + language.getString("general.error.console");
        this.messagePermission = error + language.getString("general.error.permission");
        this.messageHelp = language.getStringList("general.help");

        for (String crate : config.getConfigurationSection("crates.rewards").getKeys(false)) {
            this.crates.put(crate, new Crate(config, crate));
        }

        // If config states Crates should be enabled, register its command
        if (config.getBoolean("crates.enabled")) {
            plugin.getCommand("crate").setExecutor(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        if (args.length == 1) {
            // Do nothing if sender is not a player
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageConsole));
                return true;
            }

            // Check if player wants to see their balance
            Player player = (Player) sender;
            args[0] = args[0].toLowerCase();
            if (args[0].matches("bal|balance")) {
                // Do nothing if player does not have permission
                if (!player.hasPermission(PERMISSION_BALANCE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Get this players remaining crates
                FBPlayer fbplayer = FBPlayer.get(player.getUniqueId());
                int crates = 0;
                if (fbplayer != null) {
                    crates = fbplayer.getCrates();
                }

                // Send player their crates balance
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageBalance
                        .replace("{CRATES}", "" + crates)));

                return true;
            }

            // Check if player wants to open a crate
            if (args[0].matches("open")) {
                // Do nothing if player does not have permission
                if (!player.hasPermission(PERMISSION_OPEN)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Get this players remaining crates
                FBPlayer fbplayer = FBPlayer.get(player.getUniqueId());
                int crates = 0;
                if (fbplayer != null) {
                    crates = fbplayer.getCrates();
                }

                // Do nothing if player does not have any remaining crates
                if (crates <= 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageNotEnough));
                    return true;
                }

                // Open a crate for the player
                if (newAlgorithm) {
                    newAlgorithm(player);
                } else {
                    oldAlgorithm(player);
                }

                // Remove 1 crate from the players total balance
                fbplayer.setCrates(crates - 1);
                return true;
            }
        }

        if (args.length == 2) {
            // Check if sender wishes to view a balance
            args[0] = args[0].toLowerCase();
            if ((args[0].matches("bal|balance"))) {
                // Do nothing if the sender does not have permission
                if (!sender.hasPermission(PERMISSION_BALANCE_OTHER)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Get remaining crates for selected user
                String name = args[1];
                FBPlayer fbplayer = FBPlayer.get(name);
                int crates = 0;
                if (fbplayer != null) {
                    crates = fbplayer.getCrates();
                }

                // Send the sender the remaining crates for this selected user
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageBalanceOther
                        .replace("{NAME}", name)
                        .replace("{CRATES}", "" + crates)));

                return true;
            }
        }

        if (args.length == 3) {
            // Check if sender wishes to set the players crates
            args[0] = args[0].toLowerCase();
            if (args[0].matches("set")) {
                // Do nothing if the sender does not have permission
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Do nothing if the players data is not valid
                String name = args[1];
                FBPlayer fbplayer = FBPlayer.get(name);
                if (fbplayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageOffline));
                    return true;
                }

                // Set the players new crate amount
                int crates = getValidInteger(args[2]);
                fbplayer.setCrates(crates);

                // Send a confirmation message to the sender
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageChanged
                        .replace("{NAME}", name)
                        .replace("{CRATES}", "" + crates)));

                return true;
            }

            // Check if sender wishes to add to the players crates
            if (args[0].matches("add|give")) {
                // Do nothing if the sender does not have permission
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Do nothing if the players data is not valid
                String name = args[1].toLowerCase();
                FBPlayer fbplayer = FBPlayer.get(name);
                if (fbplayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageOffline));
                    return true;
                }

                // Add crates to the players balance
                int crates = fbplayer.getCrates() + getValidInteger(args[2]);
                fbplayer.setCrates(crates);

                // Send a confirmation message to the sender
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageChanged
                        .replace("{NAME}", name)
                        .replace("{CRATES}", "" + crates)));

                return true;
            }

            // Check if sender wishes to remove from the players crates
            if (args[0].matches("remove|rem")) {
                // Do nothing if the sender does not have permission
                if (!sender.hasPermission(PERMISSION_CHANGE)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Do nothing if the players data is not valid
                String name = args[1].toLowerCase();
                FBPlayer fbplayer = FBPlayer.get(name);
                if (fbplayer == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageOffline));
                    return true;
                }

                // Remove crates from the players balance
                int crates = fbplayer.getCrates() - getValidInteger(args[2]);
                fbplayer.setCrates(crates);

                // Send a confirmation message to the sender
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageChanged
                        .replace("{NAME}", name)
                        .replace("{CRATES}", "" + crates)));

                return true;
            }

            // Check if the sender wishes to pay a player crates
            if (args[0].matches("pay")) {
                // Do nothing if sender does not have permission
                if (!sender.hasPermission(PERMISSION_PAY)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePermission));
                    return true;
                }

                // Do nothing if sender is not a player
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageConsole));
                    return true;
                }

                // Do nothing if the players data is not valid
                @SuppressWarnings("deprecation")
                Player receiver = Bukkit.getPlayer(args[1]);
                Player player = (Player) sender;
                FBPlayer fbplayerReceiver = FBPlayer.get(args[1]);
                FBPlayer fbplayerSender = FBPlayer.get(player.getUniqueId());
                if (receiver == null || fbplayerReceiver == null || fbplayerSender == null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageOffline));
                    return true;
                }

                // Do nothing if the specified amount is smaller than 1
                int crates = getValidInteger(args[2]);
                if (crates < 1) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageInvalid));
                    return true;
                }

                // Do nothing if the player does not have enough crates
                if (fbplayerSender.getCrates() < crates) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', messageNotEnough));
                    return true;
                }

                // Pay receiver the amount specified in crates by the sender
                fbplayerSender.setCrates(fbplayerSender.getCrates() - crates);
                fbplayerReceiver.setCrates(fbplayerReceiver.getCrates() + crates);

                // Send confirmation messages to both sender and receiver
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePaymentSent
                        .replace("{CRATES}", String.valueOf(crates))
                        .replace("{NAME}", receiver.getName())));

                receiver.sendMessage(ChatColor.translateAlternateColorCodes('&', messagePaymentReceived
                        .replace("{CRATES}", String.valueOf(crates))
                        .replace("{NAME}", player.getName())));

                return true;
            }
        }

        // The senders syntax must have been wrong, sending them the help message
        for (String msg : this.messageHelp) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }

        return true;
    }

    private void newAlgorithm(Player player) {
        Random random = new Random();

        // Iterate through all listed crates
        for (String reward : crates.keySet()) {
            // Do nothing with this crate if there was no chance of getting it
            double chance = 1.0D / Double.parseDouble(reward);
            if (chance < random.nextDouble()) {
                continue;
            }

            // Execute all commands for this crate
            String name = player.getName();
            List<String> commands = crates.get(reward).getCommands();
            for (String cmd : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
            }

            // Send player the message attached to this crate
            String message = crates.get(reward).getMessage();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
        }
    }

    private void oldAlgorithm(Player player) {
        // Attempt to get any random crate listed
        Random random = new Random();
        int rewards = crates.size();
        int reward = random.nextInt(rewards);
        String path = String.valueOf(crates.keySet().toArray()[reward].toString());

        // Execute all commands for this crate
        String name = player.getName();
        List<String> commands = crates.get(path).getCommands();
        for (String cmd : commands) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("{NAME}", name));
        }

        // Send player the message attached to this crate
        String message = crates.get(path).getMessage();
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{NAME}", name)));
    }

    private int getValidInteger(String integer) {
        // Attempt to find a valid integer from a string
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}