package org.originmc.fbasics.factions.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class FactionsHelperImpl implements FactionsHelper {

    public boolean isInTerritory(Location location) {
        return false;
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        return false;
    }

    public boolean isInTerritory(Player player, Location location, List<String> factions) {
        return false;
    }

}