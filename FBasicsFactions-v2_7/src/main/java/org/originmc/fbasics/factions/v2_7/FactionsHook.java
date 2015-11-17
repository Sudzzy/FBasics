package org.originmc.fbasics.factions.v2_7;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.fbasics.factions.api.FactionsMode;
import org.originmc.fbasics.factions.api.IFactionsHook;

import java.util.ArrayList;
import java.util.List;

public final class FactionsHook implements IFactionsHook {

    @Override
    public boolean isWilderness(Location location) {
        return BoardColl.get().getFactionAt(PS.valueOf(location)).isNone();
    }

    @Override
    public boolean hasAccess(Player player, List<String> factions, FactionsMode mode) {
        return hasAccess(player, player.getLocation(), factions, mode);
    }

    @Override
    public boolean hasAccess(Player player, Location location, List<String> factions, FactionsMode mode) {
        // Grab all relevant factions data for this specific request.
        Faction locationFaction = BoardColl.get().getFactionAt(PS.valueOf(location));
        MPlayer mplayer = MPlayer.get(player);
        boolean whitelist = mode == FactionsMode.WHITELIST;

        // Return true if factions list has this specific faction declared when using whitelist. Opposite for blacklist.
        if (factions.contains(ChatColor.stripColor(locationFaction.getName()))) return whitelist;

        // Attempt to parse all the faction relations in this factions list.
        List<Rel> relations = new ArrayList<>();
        for (String faction : factions) {
            String[] data = faction.split(":");
            if (data.length == 2) {
                relations.add(Rel.parse(data[1]));
            }
        }

        // Return true if the players relation to the location faction is stated in the factions list when using
        // whitelist mode. Opposite for blacklist.
        if (relations.contains(mplayer.getRelationTo(locationFaction))) {
            return whitelist;
        }

        // Now all checks are complete, the player does not have access when using whitelist mode. Opposite for
        // blacklist.
        return !whitelist;
    }

}
