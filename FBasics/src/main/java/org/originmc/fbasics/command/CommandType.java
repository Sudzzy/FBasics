package org.originmc.fbasics.command;

import org.bukkit.command.CommandSender;
import org.originmc.fbasics.FBasics;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public enum CommandType {

    INCORRECT(CmdIncorrect.class, new String[]{"incorrect", "i"}),

    RELOAD(CmdReload.class, new String[]{"reload", "r"}),

    SAFEPROMOTE(CmdSafepromote.class, new String[]{"safepromote", "sp", "s"}),

    VERSION(CmdVersion.class, new String[]{"version", "v"}),

    WILDERNESS(CmdWilderness.class, new String[]{"wilderness", "wild", "w"});

    private static final HashMap<String, CommandType> BY_ALIAS = new HashMap<>();

    private static final String BASE_PERMISSION = "fbasics.";

    private final Class<? extends CommandExecutor> commandExecutor;

    private final String[] aliases;

    private final String permission;

    CommandType(Class<? extends CommandExecutor> commandExecutor, String[] aliases) {
        this.commandExecutor = commandExecutor;
        this.aliases = aliases;
        this.permission = BASE_PERMISSION + name().toLowerCase();
    }

    /**
     * Attempts to grab a new instance of the corresponding command executor for
     * this current command.
     *
     * @param plugin the plugin instance.
     * @param sender entity that executed this command.
     * @param args   arguments included with the command.
     * @return a new CommandExecutor instance that corresponds to the command arguments.
     */
    public static CommandExecutor fromCommand(FBasics plugin, CommandSender sender, String[] args) {
        // Return default (INCORRECT) command type if invalid arguments.
        if (args.length == 0 || !BY_ALIAS.containsKey(args[0])) {
            return newInstance(INCORRECT, plugin, sender, args);
        }

        // Return corresponding command type to inputted arguments.
        return newInstance(BY_ALIAS.get(args[0]), plugin, sender, args);
    }

    /**
     * Creates a new instance of the CommandExecutor corresponding to the
     * command type parameter.
     *
     * @param commandType command type to retrieve a new instance for.
     * @param plugin      the plugin instance.
     * @param sender      entity that executed this command.
     * @param args        arguments included with the command.
     * @return a new CommandExecutor instance that corresponds to the command type.
     */
    public static CommandExecutor newInstance(CommandType commandType, FBasics plugin, CommandSender sender, String[] args) {
        try {
            return commandType.commandExecutor
                    .getConstructor(FBasics.class, CommandSender.class, String[].class, String.class)
                    .newInstance(plugin, sender, args, commandType.permission);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        for (CommandType commandType : values()) {
            for (String alias : commandType.aliases) {
                BY_ALIAS.put(alias, commandType);
            }
        }
    }

}
