package org.originmc.fbasics.listeners;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.entity.BoardColls;
import com.massivecraft.factions.entity.UPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnderpearlListener implements Listener {

    private final boolean blocks;
    private final boolean disabled;
    private final int cooldown;
    private final int doorCooldown;
    private final FBasics plugin;
    private final String messageBlock;
    private final String messageCooldown;
    private final String messageDisabled;
    private final String messageFactions;
    private final List<String> factions;
    private final List<Material> doors = new ArrayList<Material>();
    private final String permissionEnderpearl = "fbasics.bypass.glitch.enderpearl";
    private Map<String, String> listEnderpearl = new HashMap<String, String>();

    public EnderpearlListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.disabled = config.getBoolean("patcher.enderpearls.disable-all-enderpearls");
        this.blocks = config.getBoolean("patcher.enderpearls.disable-within-block");
        this.cooldown = config.getInt("patcher.enderpearls.cooldown");
        this.doorCooldown = config.getInt("patcher.enderpearls.door-cooldown");
        this.factions = config.getStringList("patcher.enderpearls.factions-whitelist");
        this.messageBlock = error + language.getString("patcher.error.enderpearls-block");
        this.messageCooldown = info + language.getString("patcher.info.enderpearls-cooldown");
        this.messageDisabled = error + language.getString("patcher.error.enderpearls-disabled");
        this.messageFactions = error + language.getString("patcher.error.enderpearls-factions");

        for (String material : config.getStringList("patcher.enderpearls.doors"))
            this.doors.add(Material.getMaterial(material));
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (!e.getCause().equals(TeleportCause.ENDER_PEARL) || this.factions.contains("{ALL}"))
            return;

        Player player = e.getPlayer();

        if (player.hasPermission(this.permissionEnderpearl)) return;

        Location location = e.getTo();

        if (Bukkit.getPluginManager().getPlugin("Factions") != null && !isInFaction(player, location)) return;

        e.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageFactions));
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onEnderpearl(PlayerInteractEvent e) {
        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) return;

        Player player = e.getPlayer();
        Material item = player.getItemInHand().getType();

        if (player.hasPermission(this.permissionEnderpearl) || item == null || item != Material.ENDER_PEARL) return;

        if (this.disabled) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageDisabled));
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        Location location = player.getLocation();
        Material block1 = location.getBlock().getType();
        Material block2 = location.getBlock().getRelative(0, 1, 0).getType();

        if (this.blocks && (!block1.equals(Material.AIR) || !block2.equals(Material.AIR))) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageBlock));
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        if (this.listEnderpearl.containsKey(player.getName())) {
            String[] cooldownInfo = this.listEnderpearl.get(player.getName()).split("-");
            long remaining = Integer.parseInt(cooldownInfo[1]) - (System.currentTimeMillis() - Long.parseLong(cooldownInfo[0])) / 1000L;
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCooldown.replace("{REMAINING}", "" + remaining)));
            e.setCancelled(true);
            player.updateInventory();
            return;
        }

        setCooldown(player.getName(), this.cooldown);
    }

    @EventHandler
    public void onDoorInteract(PlayerInteractEvent e) {

        Action action = e.getAction();

        if (action == Action.LEFT_CLICK_AIR || action == Action.RIGHT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = e.getPlayer();

        if (player.hasPermission(this.permissionEnderpearl)) {
            return;
        }

        Material block = e.getClickedBlock().getType();

        if (this.doors.contains(block) && !this.listEnderpearl.containsKey(player.getName())) {
            setCooldown(player.getName(), this.doorCooldown);
        }
    }


    private void setCooldown(final String player, int cooldown) {

        listEnderpearl.put(player, System.currentTimeMillis() + "-" + cooldown);

        new BukkitRunnable() {
            public void run() {
                listEnderpearl.remove(player);
            }
        }.runTaskLaterAsynchronously(this.plugin, cooldown * 20);
    }


    private boolean isInFaction(Player player, Location location) {

        String version = Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion();


        if (version.startsWith("1")) {

            FLocation flocation = new FLocation(location);
            com.massivecraft.factions.Faction faction1 = Board.getFactionAt(flocation);
            com.massivecraft.factions.Faction faction2 = FPlayers.i.get(player).getFaction();


            for (String faction : this.factions) {

                if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
                    return false;
                }


                if (faction.equalsIgnoreCase(faction1.getTag()) || faction.equalsIgnoreCase(faction1.getTag().substring(2))) {
                    return false;
                }
            }
        } else if (version.startsWith("2")) {

            com.massivecraft.factions.entity.Faction faction1 = BoardColls.get().getFactionAt(PS.valueOf(location));
            com.massivecraft.factions.entity.Faction faction2 = UPlayer.get(player).getFaction();


            for (String faction : this.factions) {

                if (faction.equalsIgnoreCase("{MEMBER}") && faction1 == faction2) {
                    return false;
                }


                if (faction.equalsIgnoreCase(faction1.getName()) || faction.equalsIgnoreCase(faction1.getName().substring(2))) {
                    return false;
                }
            }
        }

        return true;
    }
}