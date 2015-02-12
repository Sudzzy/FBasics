package org.originmc.hooks.factions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface FactionsHook {
    public boolean isInTerritory(Location location);
    public boolean isInTerritory(Player player, List<String> factions);
    public boolean isInTerritory(Player player, Location location, List<String> factions);
}
