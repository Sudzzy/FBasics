package org.originmc.fbasics.task;

import org.bukkit.entity.Player;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.listeners.CommandListener;

public class WarmupTask implements Runnable {

    private final int id;
    private final double price;
    private final FBasics plugin;
    private final Player player;
    private final String command;
    private final CommandListener commandListener;

    public WarmupTask(FBasics plugin, CommandListener commandListener, Player player, String command, double price, int warmup) {
        this.plugin = plugin;
        this.id = this.plugin.getServer().getScheduler().runTaskLater(plugin, this, warmup * 20).getTaskId();
        this.price = price;
        this.player = player;
        this.command = command;
        this.commandListener = commandListener;
    }

    @Override
    public void run() {
        this.commandListener.removeWarmup(this.player.getUniqueId());

        if (!this.player.isOnline() || !this.commandListener.billPlayer(this.player, this.price)) {
            return;
        }

        this.plugin.getServer().dispatchCommand(this.player, this.command);
    }

    public void stopTask() {
        this.plugin.getServer().getScheduler().cancelTask(id);
    }
}
