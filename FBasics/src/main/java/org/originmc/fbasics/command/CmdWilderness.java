package org.originmc.fbasics.command;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.settings.WildernessSettings;
import org.originmc.fbasics.settings.WildernessWorldSettings;
import org.originmc.fbasics.task.WildernessTask;
import org.originmc.fbasics.util.MessageUtils;

public final class CmdWilderness extends CommandExecutor {

    private final FBasics plugin;

    private final WildernessSettings settings;

    public CmdWilderness(FBasics plugin, CommandSender sender, String[] args, String permission) {
        super(plugin, sender, args, permission);
        this.plugin = plugin;
        settings = plugin.getSettings().getWildernessSettings();
        setRequiresPlayer(true);
    }

    @Override
    public boolean perform() {
        // Do nothing if this module is not enabled.
        if (!settings.isEnabled()) return false;

        // Attempt to locate the destination world and the wilderness settings for this world.
        Player player = (Player) getSender();
        World world = null;
        WildernessWorldSettings worldSettings = null;
        if (settings.getWorlds().containsKey(player.getWorld().getName())) {
            world = player.getWorld();
            worldSettings = settings.getWorlds().get(world.getName());
        }

        if (world == null && !settings.getDefaultWorld().equalsIgnoreCase("none")) {
            world = Bukkit.getWorld(settings.getDefaultWorld());
            if (world != null) {
                worldSettings = settings.getWorlds().get(world.getName());
            }
        }

        // Do nothing if the world is null or the settings denies world change.
        if (world == null || worldSettings == null) {
            MessageUtils.sendMessage(player, settings.getWorldMessage());
            return true;
        }

        // Create a new wilderness task.
        MessageUtils.sendMessage(player, settings.getSearchMessage());
        new WildernessTask(plugin, player, world, worldSettings);
        return true;
    }

}
