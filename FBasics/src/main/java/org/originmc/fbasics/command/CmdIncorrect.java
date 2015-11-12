package org.originmc.fbasics.command;

import org.bukkit.command.CommandSender;
import org.originmc.fbasics.FBasics;

import static org.bukkit.ChatColor.*;

public final class CmdIncorrect extends CommandExecutor {

    public CmdIncorrect(FBasics plugin, CommandSender sender, String[] args, String permission) {
        super(plugin, sender, args, permission);
    }

    @Override
    public boolean perform() {
        return false;
    }

}
