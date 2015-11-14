package org.originmc.fbasics.task;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;
import org.originmc.fbasics.factions.api.IFactionsHook;

@Data
public final class WildernessFactionsTask implements Runnable {

    private final int x, y, z, radius;

    private final World world;

    private final IFactionsHook factions;

    private volatile State state = State.SEARCHING;

    @Override
    public void run() {
        for (int chunkX = -radius; chunkX <= radius; chunkX++) {
            for (int chunkZ = -radius; chunkZ <= radius; chunkZ++) {
                Location location = new Location(world, (chunkX << 4) + x, y, (chunkZ << 4) + z);
                if (!factions.isWilderness(location)) {
                    state = State.DENIED;
                    return;
                }
            }
        }

        state = State.ALLOWED;
    }

    public enum State {

        SEARCHING, ALLOWED, DENIED

    }

}
