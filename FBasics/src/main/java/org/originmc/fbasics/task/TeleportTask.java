package org.originmc.fbasics.task;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Data
public final class TeleportTask implements Runnable {

    private final Player player;

    private final Location location;

    @Override
    public void run() {
        player.teleport(location);
    }

}
