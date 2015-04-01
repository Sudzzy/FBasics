package org.originmc.fbasics;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Crate {

    private final String message;

    private final List<String> commands;

    public Crate(FileConfiguration config, String crate) {
        String prefix = config.getString("crates.message-prefix");
        this.message = prefix + config.getString("crates.rewards." + crate + ".message");
        this.commands = config.getStringList("crates.rewards." + crate + ".commands");
    }

    public String getMessage() {
        return this.message;
    }

    public List<String> getCommands() {
        return this.commands;
    }

}