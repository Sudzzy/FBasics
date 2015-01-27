package org.originmc.hooks.factions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface FactionsHook {
    public boolean isInTerritory(Location location);
    public boolean isInsideClaim(Player player, List<String> factions);
    public boolean isInFaction(Player player, Location location);
}
