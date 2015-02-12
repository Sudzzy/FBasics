package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnderpearlListener implements Listener {

    private static final String PERMISSION_ENDERPEARL = "fbasics.bypass.glitch.enderpearl";
    private final boolean blocks;
    private final boolean disabled;
    private final boolean correctTeleport;
    private final int cooldown;
    private final int doorCooldown;
    private final FBasics plugin;
    private final String messageBlock;
    private final String messageCooldown;
    private final String messageDisabled;
    private final String messageFactions;
    private final List<String> factions;
    private final List<Material> doors = new ArrayList<>();
    private final List<Material> hollowMaterials = new ArrayList<>();
    private final Map<String, String> enderpearlCooldowns = new HashMap<>();

    public EnderpearlListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration language = plugin.getLanguage();
        FileConfiguration materials = plugin.getMaterials();
        String error = language.getString("general.error.prefix");
        String info = language.getString("general.info.prefix");

        this.plugin = plugin;
        this.disabled = config.getBoolean("patcher.enderpearls.disable-all-enderpearls");
        this.blocks = config.getBoolean("patcher.enderpearls.disable-within-block");
        this.correctTeleport = config.getBoolean("patcher.enderpearls.correct-teleport");
        this.cooldown = config.getInt("patcher.enderpearls.cooldown");
        this.doorCooldown = config.getInt("patcher.enderpearls.door-cooldown");
        this.factions = config.getStringList("patcher.enderpearls.factions-whitelist");
        this.messageBlock = error + language.getString("patcher.error.enderpearls-block");
        this.messageCooldown = info + language.getString("patcher.info.enderpearls-cooldown");
        this.messageDisabled = error + language.getString("patcher.error.enderpearls-disabled");
        this.messageFactions = error + language.getString("patcher.error.enderpearls-factions");

        for (String material : materials.getStringList("doors")) {
            this.doors.add(Material.getMaterial(material));
        }

        for (String hollowMaterials : materials.getStringList("hollow-materials")) {
            this.hollowMaterials.add(Material.getMaterial(hollowMaterials));
        }

        if (config.getBoolean("patcher.enderpearls.enabled")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Enderpearls module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (!event.getCause().equals(TeleportCause.ENDER_PEARL)) {
            return;
        }

        Player player = event.getPlayer();

        if (!this.factions.contains("{ALL}") && !player.hasPermission(PERMISSION_ENDERPEARL)) {
            Location location = event.getTo();

            if (plugin.getFactionsHook() != null &&
                    this.plugin.getFactionsHook().isInTerritory(player, location, this.factions)) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageFactions));
                player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
            }
        }

        if (this.correctTeleport) {
            Location toLocation = event.getTo();
            Block toBlock = toLocation.getBlock();
            double excess = toLocation.getY() - (int) toLocation.getY();

            if (this.hollowMaterials.contains(toBlock.getType())) {
                event.setTo(event.getTo().subtract(0, excess, 0));
            }

            if (!this.hollowMaterials.contains(toBlock.getRelative(BlockFace.UP).getType())) {
                event.setTo(toLocation.subtract(0, 1, 0));
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Action action = event.getAction();
        Player player = event.getPlayer();
        Material material = player.getItemInHand().getType();

        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) &&
                !player.hasPermission(PERMISSION_ENDERPEARL) &&
                this.doors.contains(event.getClickedBlock().getType()) &&
                !this.enderpearlCooldowns.containsKey(player.getName())) {
            this.enderpearlCooldowns.put(player.getName(), System.currentTimeMillis() + "-" + this.doorCooldown);
        }

        if (player.hasPermission(PERMISSION_ENDERPEARL) || material == null || material != Material.ENDER_PEARL) {
            return;
        }

        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
        } else if (action.equals(Action.RIGHT_CLICK_AIR)) {

            if (this.disabled) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageDisabled));
                event.setCancelled(true);
                player.updateInventory();
                return;
            }

            Location location = player.getLocation();
            Material feet = location.getBlock().getType();
            Material head = location.getBlock().getRelative(0, 1, 0).getType();

            if (this.blocks && (!this.hollowMaterials.contains(feet) || !this.hollowMaterials.contains(head))) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageBlock));
                event.setCancelled(true);
                player.updateInventory();
                return;
            }

            if (this.enderpearlCooldowns.containsKey(player.getName())) {
                String[] cooldownInfo = this.enderpearlCooldowns.get(player.getName()).split("-");
                long remaining = Integer.parseInt(cooldownInfo[1]) - (System.currentTimeMillis() - Long.parseLong(cooldownInfo[0])) / 1000L;

                if (remaining < 0) {
                    this.enderpearlCooldowns.remove(player.getName());
                } else {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.messageCooldown.replace("{REMAINING}", "" + remaining)));
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
            }

            this.enderpearlCooldowns.put(player.getName(), System.currentTimeMillis() + "-" + this.cooldown);
        }
    }
}
