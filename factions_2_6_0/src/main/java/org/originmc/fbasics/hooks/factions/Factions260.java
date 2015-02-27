package org.originmc.fbasics.hooks.factions;

import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class Factions260 implements FactionsHook {

    private static final String PERMISSION_TERRITORY = "fbasics.bypass.commands.territory";

    public boolean isInTerritory(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColls.get().getFactionAt(ps);
        return faction.isNone();
    }

    public boolean isInTerritory(Player player, List<String> factions) {
        if (factions.isEmpty() || player.hasPermission(PERMISSION_TERRITORY)) {
            return false;
        }

        PS ps = com.massivecraft.massivecore.ps.PS.valueOf(player.getLocation());
        Faction faction1 = BoardColls.get().getFactionAt(ps);
        Faction faction2 = UPlayer.get(player).getFaction();

        for (String faction : factions) {
            if ((faction.equalsIgnoreCase("{MEMBER}") &&
                    faction1 == faction2) ||
                    faction.equalsIgnoreCase(faction1.getName()) ||
                    faction.equalsIgnoreCase(faction1.getName().substring(2))) {
                return false;
            }
        }

        return true;
    }

    public boolean isInTerritory(Player player, Location location, List<String> factions) {
        PS ps = PS.valueOf(location);
        Faction faction1 = BoardColls.get().getFactionAt(ps);
        Faction faction2 = UPlayer.get(player).getFaction();

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
