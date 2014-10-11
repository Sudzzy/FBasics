package org.originmc.fbasics.settings;

import java.util.List;

public class LanguageSettings {

    /**
     * General
     */
    public static List<String> help;
    public static String console;
    public static String permission;
    public static String invalidPlayer;
    public static String reload;


    /**
     * Anti Looter
     */
    public static String antiLooterDropped;
    public static String antiLooterProtected;
    public static String antiLooterTimerFinish;


    /**
     * Commands
     */
    public static String commandsBlock;
    public static String commandsCancelled;
    public static String commandsCooldown;
    public static String commandsFaction;
    public static String commandsInsufficientFunds;
    public static String commandsPaid;
    public static String commandsWarmup;
    public static String commandsWarmupDouble;


    /**
     * Crates
     */
    public static String cratesBalance;
    public static String cratesBalanceOther;
    public static String cratesChanged;
    public static String cratesPaymentSent;
    public static String cratesPaymentReceived;
    public static String cratesNotEnough;
    public static String cratesInvalid;


    /**
     * Patcher
     */
    public static String cropBlock;
    public static String enderpearlsCooldown;
    public static String enderpearlsDisabled;
    public static String enderpearlsFactions;
    public static String enderpearlsBlock;
    public static String netherCancelled;


    /**
     * Wilderness
     */
    public static String wildernessFailed;
    public static String wildernessSuccess;
    public static String wildernessWorld;


    public static void loadLanguageSettings() {

        String error = SettingsManager.getLanguage().getString("general.error.prefix");
        String info = SettingsManager.getLanguage().getString("general.info.prefix");
        help = SettingsManager.getLanguage().getStringList("general.help");
        console = error + SettingsManager.getLanguage().getString("general.error.console");
        permission = error + SettingsManager.getLanguage().getString("general.error.permission");
        invalidPlayer = error + SettingsManager.getLanguage().getString("general.error.player");
        reload = info + SettingsManager.getLanguage().getString("general.info.reload");


        antiLooterDropped = info + SettingsManager.getLanguage().getString("anti-looter.info.dropped");
        antiLooterProtected = info + SettingsManager.getLanguage().getString("anti-looter.info.protected");
        antiLooterTimerFinish = info + SettingsManager.getLanguage().getString("anti-looter.info.unprotected");


        commandsBlock = error + SettingsManager.getLanguage().getString("commands.error.block");
        commandsCancelled = info + SettingsManager.getLanguage().getString("commands.info.cancelled");
        commandsCooldown = info + SettingsManager.getLanguage().getString("commands.info.cooldown");
        commandsFaction = error + SettingsManager.getLanguage().getString("commands..error.faction");
        commandsInsufficientFunds = error + SettingsManager.getLanguage().getString("commands.error.funds");
        commandsPaid = info + SettingsManager.getLanguage().getString("commands.info.paid");
        commandsWarmup = info + SettingsManager.getLanguage().getString("commands.info.warmup");
        commandsWarmupDouble = error + SettingsManager.getLanguage().getString("commands.error.warmup");


        cratesNotEnough = error + SettingsManager.getLanguage().getString("crates.error.balance");
        cratesInvalid = error + SettingsManager.getLanguage().getString("crates.error.invalid");
        cratesBalance = info + SettingsManager.getLanguage().getString("crates.info.balance");
        cratesBalanceOther = info + SettingsManager.getLanguage().getString("crates.info.balance-other");
        cratesChanged = info + SettingsManager.getLanguage().getString("crates.info.changed");
        cratesPaymentSent = info + SettingsManager.getLanguage().getString("crates.info.payment-sent");
        cratesPaymentReceived = info + SettingsManager.getLanguage().getString("crates.info.payment-received");


        cropBlock = error + SettingsManager.getLanguage().getString("patcher.error.crop-place");
        enderpearlsCooldown = info + SettingsManager.getLanguage().getString("patcher.info.enderpearls-cooldown");
        enderpearlsDisabled = error + SettingsManager.getLanguage().getString("patcher.error.enderpearls-disabled");
        enderpearlsFactions = error + SettingsManager.getLanguage().getString("patcher.error.enderpearls-factions");
        enderpearlsBlock = error + SettingsManager.getLanguage().getString("patcher.error.enderpearls-block");
        netherCancelled = error + SettingsManager.getLanguage().getString("patcher.error.nether-cancelled");


        wildernessFailed = error + SettingsManager.getLanguage().getString("wilderness.error.failed");
        wildernessSuccess = info + SettingsManager.getLanguage().getString("wilderness.info.success");
        wildernessWorld = error + SettingsManager.getLanguage().getString("wilderness.error.world");

    }
}