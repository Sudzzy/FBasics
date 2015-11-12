package org.originmc.fbasics.task;

import lombok.Data;
import org.originmc.fbasics.FBasics;

@Data
public final class SaveDatabaseTask implements Runnable {

    private final FBasics plugin;

    public SaveDatabaseTask(FBasics plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.saveDatabase();
    }

}
