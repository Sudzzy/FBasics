package org.originmc.fbasics;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.fbasics.factions.api.FactionsHelper;

import java.util.List;

public final class FactionsManager {

    private final FactionsHelper helper;

    public FactionsManager(FactionsHelper helper) {
        this.helper = helper;
    }

    public boolean isInTerritory(Location location) {
        return helper.isInTerritory(location);
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        return helper.isInTerritory(player, factions);
    }

    public boolean isInTerritory(Player player, Location location, List<String> factions) {
        return helper.isInTerritory(player, location, factions);
    }

}