package org.originmc.fbasics.task;

import lombok.Data;
import org.originmc.fbasics.FBasics;

@Data
public final class CleanDatabaseTask implements Runnable {

    private final FBasics plugin;

    @Override
    public void run() {
        plugin.cleanDatabase();
    }

}
