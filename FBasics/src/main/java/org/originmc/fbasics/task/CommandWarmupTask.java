package org.originmc.fbasics.task;

import lombok.Data;
import lombok.ToString;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.originmc.fbasics.CommandModifier;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.event.CommandModifierEvent;
import org.originmc.fbasics.settings.CommandModifierGroupSettings;
import org.originmc.fbasics.util.DurationUtils;
import org.originmc.fbasics.util.MessageUtils;

import java.lang.ref.WeakReference;

@Data
@ToString(exclude = "modifier")
public final class CommandWarmupTask implements Runnable {

    private final int taskId;

    private final CommandModifierGroupSettings settings;

    private final CommandModifier modifier;

    private final WeakReference<Player> player;

    private final FBasics plugin;

    private boolean running = true;

    private String command;

    public CommandWarmupTask(FBasics plugin, Player player, CommandModifierGroupSettings settings, CommandModifier modifier, String command) {
        this.taskId = Bukkit.getScheduler().runTaskTimer(plugin, this, 10, 10).getTaskId();
        this.plugin = plugin;
        this.player = new WeakReference<>(player);
        this.settings = settings;
        this.modifier = modifier;
        this.command = command;
    }

    @Override
    public void run() {
        // Cancel the task if the player is no longer online.
        Player player = this.player.get();
        if (player == null || !player.isOnline()) {
            cancel(false);
            return;
        }

        // Do nothing if warmup is still not complete.
        if (DurationUtils.calculateRemaining(modifier.getWarmup()) > 0) {
            return;
        }

        // Tell plugin this warmup is no longer effective.
        running = false;

        // Create then execute the command modifier event.
        CommandModifierEvent event = new CommandModifierEvent(player, settings, modifier, command);
        Bukkit.getPluginManager().callEvent(event);

        // Replace the message with what message returned.
        command = event.getCommand();

        // Do not dispatch command if modifier event was cancelled.
        if (event.isCancelled()) {
            cancel(false);
            return;
        }

        // Dispatch the command.
        Bukkit.dispatchCommand(player, command.substring(1));

        // Now fully cancel and remove this task.
        cancel(false);
    }

    public void cancel(boolean inform) {
        // Inform player the task has been cancelled.
        Player player = this.player.get();
        if (inform && player != null && player.isOnline()) {
            MessageUtils.sendMessage(player, plugin.getSettings().getCommandSettings().getWarmupFailedMessage());
        }

        running = false;
        modifier.setTask(null);
        Bukkit.getScheduler().cancelTask(taskId);
    }

}
