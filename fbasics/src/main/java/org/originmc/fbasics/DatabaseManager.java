package org.originmc.fbasics;

public final class DatabaseManager {

    private FBasics plugin;

    public DatabaseManager(FBasics plugin) {
        this.plugin = plugin;
    }

    public int getCrates(String name) {
        name = name.toLowerCase();

        if (!this.plugin.crates.containsKey(name)) {
            return 0;
        }

        return this.plugin.crates.get(name);
    }

    public void setCrates(String name, int crates) {
        name = name.toLowerCase();
        this.plugin.crates.put(name, crates);
        this.plugin.updateCrates.add(name);
    }
}
