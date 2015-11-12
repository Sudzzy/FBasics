package org.originmc.fbasics.factions.v2_6;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.fbasics.factions.api.FactionsMode;
import org.originmc.fbasics.factions.api.IFactionsHook;

import java.util.List;

public final class FactionsHook implements IFactionsHook {

    @Override
    public boolean isWilderness(Location location) {
        return BoardColls.get().getFactionAt(PS.valueOf(location)).isNone();
    }

    @Override
    public boolean hasAccess(Player player, List<String> factions, FactionsMode mode) {
        return hasAccess(player, player.getLocation(), factions, mode);
    }

    @Override
    public boolean hasAccess(Player player, Location location, List<String> factions, FactionsMode mode) {
        // Grab all relevant factions data for this specific request.
        Faction locationFaction = BoardColls.get().getFactionAt(PS.valueOf(location));
        UPlayer uplayer = UPlayer.get(player);
        boolean whitelist = mode == FactionsMode.WHITELIST;

        // Return true if factions list has this specific faction declared when using whitelist. Opposite for blacklist.
        if (factions.contains(ChatColor.stripColor(locationFaction.getName()))) return whitelist;

        // Return true if the players relation to the location faction is stated in the factions list when using
        // whitelist mode. Opposite for blacklist.
        switch (uplayer.getRelationTo(locationFaction)) {
            case LEADER:
                if (factions.contains("Rel:Leader")) return whitelist;
                break;
            case OFFICER:
                if (factions.contains("Rel:Officer")) return whitelist;
                break;
            case MEMBER:
                if (factions.contains("Rel:Member")) return whitelist;
                break;
            case RECRUIT:
                if (factions.contains("Rel:Recruit")) return whitelist;
                break;
            case ALLY:
                if (factions.contains("Rel:Ally")) return whitelist;
                break;
            case TRUCE:
                if (factions.contains("Rel:Truce")) return whitelist;
                break;
            case NEUTRAL:
                if (factions.contains("Rel:Neutral")) return whitelist;
                break;
            case ENEMY:
                if (factions.contains("Rel:Enemy")) return whitelist;
                break;
        }

        // Now all checks are complete, the player does not have access when using whitelist mode. Opposite for
        // blacklist.
        return !whitelist;
    }

}
