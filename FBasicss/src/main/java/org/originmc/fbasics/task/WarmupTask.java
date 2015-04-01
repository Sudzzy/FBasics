package org.originmc.fbasics.task;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.listeners.CommandListener;

public class WarmupTask implements Runnable {

    private final int id;

    private final double price;

    private final Player player;

    private final String command;

    private final CommandListener commandListener;

    public WarmupTask(FBasics plugin, CommandListener commandListener, Player player, String command, double price, int warmup) {
        this.id = Bukkit.getScheduler().runTaskLater(plugin, this, warmup * 20).getTaskId();
        this.price = price;
        this.player = player;
        this.command = command;
        this.commandListener = commandListener;
    }

    @Override
    public void run() {
        // Warmup finished, therefore should be removed
        commandListener.removeWarmup(player.getUniqueId());

        // Do nothing if player is no longer online
        if (!player.isOnline()) return;

        // Do nothing if player cannot afford the command
        if (!commandListener.billPlayer(player, price)) return;

        // Execute the command
        Bukkit.dispatchCommand(player, command);
    }

    public void stopTask() {
        Bukkit.getScheduler().cancelTask(id);
    }

}