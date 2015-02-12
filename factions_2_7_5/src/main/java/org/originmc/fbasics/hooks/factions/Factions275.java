package org.originmc.fbasics.hooks.factions;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.originmc.hooks.factions.FactionsHook;

import java.util.List;

public class Factions275 implements FactionsHook {

    private static final String PERMISSION_TERRITORY = "fbasics.bypass.commands.territory";
    private final String msgFaction;
    private final List<String> factions;

    public Factions275(String msgFaction, List<String> factions) {
        this.msgFaction = msgFaction;
        this.factions = factions;
    }

    public boolean isInTerritory(Location location) {
        PS ps = PS.valueOf(location);
        Faction faction = BoardColl.get().getFactionAt(ps);
        return faction.isNone();
    }

    public boolean isInsideClaim(Player player, List<String> factions) {
        if (factions.isEmpty() || player.hasPermission(PERMISSION_TERRITORY))
            return false;

        PS ps = com.massivecraft.massivecore.ps.PS.valueOf(player.getLocation());
        Faction faction1 = BoardColl.get().getFactionAt(ps);
        Faction faction2 = MPlayer.get(player).getFaction();

        for (String f : factions)
            if ((f.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) || f.equalsIgnoreCase(faction1.getName()) || f.equalsIgnoreCase(faction1.getName().substring(2)))
                return false;

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', msgFaction));
        return true;
    }

    public boolean isInFaction(Player player, Location location) {
        PS ps = PS.valueOf(location);
        Faction faction1 = BoardColl.get().getFactionAt(ps);
        Faction faction2 = MPlayer.get(player).getFaction();

        for (String faction : factions) {
            if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) return false;
            if (faction.equalsIgnoreCase(faction1.getName()) || faction.equalsIgnoreCase(faction1.getName().substring(2)))
                return false;
        }

        return true;
    }
}
