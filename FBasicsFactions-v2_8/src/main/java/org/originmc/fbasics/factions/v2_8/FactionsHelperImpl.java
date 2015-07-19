package org.originmc.fbasics.factions.v2_8;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.fbasics.factions.api.FactionsHelper;

import java.util.List;

public class FactionsHelperImpl implements FactionsHelper {

    private static final String PERMISSION_TERRITORY = "fbasics.bypass.commands.territory";

    public boolean isInTerritory(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColl.get().getFactionAt(ps);
        return !faction.isNone();
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        if (factions.isEmpty() || player.hasPermission(PERMISSION_TERRITORY)) {
            return false;
        }

        PS ps = PS.valueOf(player.getLocation());
        Faction faction1 = BoardColl.get().getFactionAt(ps);
        Faction faction2 = MPlayer.get(player).getFaction();

        for (String faction : factions) {
            if ((faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) ||
                    faction.equalsIgnoreCase(faction1.getName()) ||
                    faction.equalsIgnoreCase(faction1.getName().substring(2))) {
                return false;
            }
        }

        return true;
    }

    public boolean isInTerritory(Player player, Location location, List<String> factions) {
        PS ps = PS.valueOf(location);
        Faction faction1 = BoardColl.get().getFactionAt(ps);
        Faction faction2 = MPlayer.get(player).getFaction();

        for (String faction : factions) {
            if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
                return false;
            }

            if (faction.equalsIgnoreCase(faction1.getName()) ||
                    faction.equalsIgnoreCase(faction1.getName().substring(2))) {
                return false;
            }
        }

        return true;
    }
}
