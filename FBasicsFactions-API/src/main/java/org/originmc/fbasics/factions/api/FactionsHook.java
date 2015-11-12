package org.originmc.fbasics.factions.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public final class FactionsHook implements IFactionsHook {

    @Override
    public boolean isWilderness(Location location) {
        return true;
    }

    @Override
    public boolean hasAccess(Player player, List<String> factions, FactionsMode mode) {
        return true;
    }

    @Override
    public boolean hasAccess(Player player, Location location, List<String> factions, FactionsMode mode) {
        return true;
    }

}
