package org.originmc.fbasics;

import org.bukkit.entity.Player;

import java.util.*;

public class FBPlayer {

    private static final List<FBPlayer> fbplayers = new ArrayList<>();

    private int crates;

    private final String name;

    private final UUID uniqueId;

    private final Map<CommandEditor, Long> cooldowns = new HashMap<>();

    public FBPlayer(Player player) {
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        fbplayers.add(this);
    }

    public FBPlayer(String line) {
        String[] playerData = line.split(",");
        this.uniqueId = UUID.fromString(playerData[0]);
        this.name = playerData[1];
        this.crates = Integer.valueOf(playerData[2]);

        for (int i = 0; i < playerData.length; i++) {
            if (i < 3) {
                continue;
            }

            String[] cooldownData = playerData[i].split(":");
            CommandEditor commandEditor = CommandEditor.getByEditor(cooldownData[0]);

            if (commandEditor != null) {
                this.cooldowns.put(commandEditor, Long.valueOf(cooldownData[1]));
            }
        }

        fbplayers.add(this);
    }

    public static FBPlayer get(UUID uuid) {
        for (FBPlayer fbplayer : fbplayers) {
            if (fbplayer.getUniqueId().equals(uuid)) {
                return fbplayer;
            }
        }
        return null;
    }

    public static FBPlayer get(String name) {
        for (FBPlayer fbplayer : fbplayers) {
            if (fbplayer.getName().equalsIgnoreCase(name)) {
                return fbplayer;
            }
        }
        return null;
    }

    public static List<FBPlayer> getFBPlayers() {
        return fbplayers;
    }

    public void remove() {
        fbplayers.remove(this);
    }

    public int getCrates() {
        return crates;
    }

    public void setCrates(int crates) {
        this.crates = crates;
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public Map<CommandEditor, Long> getCooldowns() {
        return cooldowns;
    }

    public long getCooldown(CommandEditor commandEditor) {
        if (this.cooldowns.containsKey(commandEditor)) {
            return this.cooldowns.get(commandEditor);
        }
        return 0;
    }

    public void setCooldown(CommandEditor commandEditor) {
        this.cooldowns.put(commandEditor, System.currentTimeMillis());
    }

    public void removeCooldown(CommandEditor commandEditor) {
        this.cooldowns.remove(commandEditor);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.uniqueId.toString());
        sb.append(",");
        sb.append(this.name);
        sb.append(",");
        sb.append(this.crates);

        for (CommandEditor commandEditor : this.cooldowns.keySet()) {
            sb.append(",");
            sb.append(commandEditor.toString());
            sb.append(":");
            sb.append(this.cooldowns.get(commandEditor).toString());
        }

        return sb.toString();
    }

}