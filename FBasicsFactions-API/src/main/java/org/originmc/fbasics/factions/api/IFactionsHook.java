package org.originmc.fbasics.factions.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public interface IFactionsHook {

    /**
     * Checks to see if a location is claimed by Wilderness.
     *
     * @param location the location to check for.
     * @return true if the location is claimed by Wilderness.
     */
    boolean isWilderness(Location location);

    /**
     * Checks to see if a player has access to the faction at their own location while providing a list of factions the
     * player is accessed to in this specific request.
     *
     * @param player   the player to check for.
     * @param factions the list of factions to check for.
     * @param mode     how the list will be treated.
     * @return true if player can execute the command.
     */
    boolean hasAccess(Player player, List<String> factions, FactionsMode mode);

    /**
     * Checks to see if a player has access to the faction at a specific location while providing a list of factions the
     * player is accessed to in this specific request.
     *
     * @param player   the player to check for.
     * @param location the location to check for.
     * @param factions the list of factions to check for.
     * @param mode     how the list will be treated.
     * @return true if player can enderpearl.
     */
    boolean hasAccess(Player player, Location location, List<String> factions, FactionsMode mode);

}
