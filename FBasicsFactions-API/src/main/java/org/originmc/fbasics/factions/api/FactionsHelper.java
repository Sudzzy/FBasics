package org.originmc.fbasics.factions.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface FactionsHelper {

    boolean isInTerritory(Location location);

    boolean isInTerritory(Player player, List<String> factions);

    boolean isInTerritory(Player player, Location location, List<String> factions);

}