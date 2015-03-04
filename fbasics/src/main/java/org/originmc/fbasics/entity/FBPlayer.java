package org.originmc.fbasics.entity;

import org.bukkit.entity.Player;

import java.util.*;

public class FBPlayer {

    private static final List<FBPlayer> fbPlayers = new ArrayList<>();
    private int crates;
    private final String name;
    private final UUID uniqueId;
    private final Map<CommandEditor, Long> cooldowns = new HashMap<>();

    public FBPlayer(Player player) {
        this.name = player.getName();
        this.uniqueId = player.getUniqueId();
        fbPlayers.add(this);
    }

    public FBPlayer(String line) {
        String[] playerData = line.split(",");
        this.uniqueId = UUID.fromString(playerData[0]);
        this.name = playerData[1];

        for (int i = 0; i < playerData.length - 1; i++) {
            if (i < 2) {
                continue;
            }

            String[] cooldownData = playerData[i].split(":");
            CommandEditor commandEditor = CommandEditor.getByEditor(cooldownData[0]);

            if (commandEditor != null) {
                this.cooldowns.put(commandEditor, Long.valueOf(cooldownData[1]));
            }
        }

        fbPlayers.add(this);
    }

    public static FBPlayer get(UUID uuid) {
        for (FBPlayer fbPlayer : fbPlayers) {
            if (fbPlayer.getUniqueId().equals(uuid)) {
                return fbPlayer;
            }
        }
        return null;
    }

    public static FBPlayer get(String name) {
        for (FBPlayer fbPlayer : fbPlayers) {
            if (fbPlayer.getName().equalsIgnoreCase(name)) {
                return fbPlayer;
            }
        }
        return null;
    }

    public static List<FBPlayer> getFbPlayers() {
        return fbPlayers;
    }

    public void remove() {
        fbPlayers.remove(this);
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

        for (CommandEditor commandEditor : this.cooldowns.keySet()) {
            sb.append(",");
            sb.append(commandEditor.toString());
            sb.append(":");
            sb.append(this.cooldowns.get(commandEditor).toString());
        }

        return sb.toString();
    }
}
