package org.originmc.fbasics.hooks.factions;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.Faction;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class Factions182 implements FactionsHook {

    private static final String PERMISSION_TERRITORY = "fbasics.bypass.commands.territory";

    public boolean isInTerritory(Location location) {
        FLocation flocation = new FLocation(location);
        Faction faction = Board.getFactionAt(flocation);
        return faction.isNone();
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        if (factions.isEmpty() || player.hasPermission(PERMISSION_TERRITORY)) {
            return false;
        }

        FLocation flocation = new FLocation(player.getLocation());
        Faction faction1 = Board.getFactionAt(flocation);
        Faction faction2 = FPlayers.i.get(player).getFaction();

        for (String faction : factions) {
            if ((faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) ||
                    faction.equalsIgnoreCase(faction1.getTag()) ||
                    faction.equalsIgnoreCase(faction1.getTag().substring(2))) {
                return false;
            }
        }

        return true;
    }

    public boolean isInTerritory(Player player, Location location, List<String> factions) {
        FLocation flocation = new FLocation(location);
        Faction faction1 = Board.getFactionAt(flocation);
        Faction faction2 = FPlayers.i.get(player).getFaction();

        for (String faction : factions) {
            if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
                return false;
            }

            if (faction.equalsIgnoreCase(faction1.getTag()) ||
                    faction.equalsIgnoreCase(faction1.getTag().substring(2))) {
                return false;
            }
        }

        return true;
    }
}
