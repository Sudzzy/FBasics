package org.originmc.fbasics.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.originmc.fbasics.CommandModifier;
import org.originmc.fbasics.settings.CommandModifierGroupSettings;

public final class CommandModifierEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final CommandModifier modifier;

    private final CommandModifierGroupSettings settings;

    private boolean cancelled;

    private String command;

    public CommandModifierEvent(Player player, CommandModifierGroupSettings settings, CommandModifier modifier, String command) {
        super(player);
        this.settings = settings;
        this.modifier = modifier;
        this.command = command;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public CommandModifierGroupSettings getSettings() {
        return settings;
    }

    public CommandModifier getModifier() {
        return modifier;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

}
