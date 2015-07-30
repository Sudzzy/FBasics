package org.originmc.fbasics.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.util.DurationUtils;

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
        // Load all settings for the Enderpearls module
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

        // Register Enderpearls events to the server if stated in the config
        if (config.getBoolean("patcher.enderpearls.enabled")) {
            Bukkit.getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Enderpearls module loaded");
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void denyTeleport(PlayerTeleportEvent event) {
        // Do nothing if not teleported by an enderpearl
        if (!event.getCause().equals(TeleportCause.ENDER_PEARL)) return;

        // Do nothing if config states teleportation into all territories is allowed
        Player player = event.getPlayer();
        if (factions.contains("{ALL}")) return;

        // Do nothing if player has permission
        if (player.hasPermission(PERMISSION_ENDERPEARL)) return;

        // Do nothing if location is not inside territory
        Location location = event.getTo();
        if (!plugin.getFactionsManager().isInTerritory(player, location, factions)) return;

        // Deny teleportation as it is inside another factions territory
        event.setCancelled(true);

        // Give player back their enderpearl
        player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));

        // Send player a message
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageFactions));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void corretTeleport(PlayerTeleportEvent event) {
        // Do nothing if plugin should not safely teleport enderpearls
        if (!correctTeleport) return;

        // Do nothing if not teleported by an enderpearl
        if (!event.getCause().equals(TeleportCause.ENDER_PEARL)) return;

        // Calculate safe teleportation location
        Location to = event.getTo();
        Block block = to.getBlock();
        double excess = to.getY() - (int) to.getY();
        if (hollowMaterials.contains(block.getType())) {
            event.setTo(event.getTo().subtract(0, excess, 0));
        }

        if (!hollowMaterials.contains(block.getRelative(BlockFace.UP).getType())) {
            event.setTo(to.subtract(0, 1, 0));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void setDoorCooldown(PlayerInteractEvent event) {
        // Do nothing if player has not right clicked a block
        Action action = event.getAction();
        if (!action.equals(Action.RIGHT_CLICK_BLOCK)) return;

        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_ENDERPEARL)) return;

        // Do nothing if player did not click a door
        if (!doors.contains(event.getClickedBlock().getType())) return;

        // Give player an enderpearl cooldown
        enderpearlCooldowns.put(player.getName(), System.currentTimeMillis() + "-" + doorCooldown);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void setEnderpearlCooldown(PlayerInteractEvent event) {
        // Do nothing if player has permission
        Player player = event.getPlayer();
        if (player.hasPermission(PERMISSION_ENDERPEARL)) return;

        // Do nothing if material is null
        Material material = player.getItemInHand().getType();
        if (material == null) return;

        // Do nothing if material is not an enderpearl
        if (material != Material.ENDER_PEARL) return;

        // Deny enderpearl if player clicked a block
        Action action = event.getAction();
        if (action.equals(Action.RIGHT_CLICK_BLOCK)) {
            event.setCancelled(true);
            return;
        }

        // Do nothing if player did not right click air
        if (!action.equals(Action.RIGHT_CLICK_AIR)) return;

        // Check if config is set to disable all enderpearls
        if (disabled) {
            // Deny enderpearl
            event.setCancelled(true);

            // Send player a message
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageDisabled));
            return;
        }

        // Check if player is within a block and config denies enderpearls within blocks
        Location location = player.getLocation();
        Material feet = location.getBlock().getType();
        Material head = location.getBlock().getRelative(0, 1, 0).getType();
        if (blocks && (!hollowMaterials.contains(feet) || !hollowMaterials.contains(head))) {
            // Deny enderpearl
            event.setCancelled(true);

            // Send player a message
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageBlock));
            return;
        }

        // Check if player already has an enderpearl cooldown
        if (enderpearlCooldowns.containsKey(player.getName())) {
            // Find the remaining cooldown duration
            String[] cooldownInfo = enderpearlCooldowns.get(player.getName()).split("-");
            long remaining = Integer.parseInt(cooldownInfo[1]) -
                    (System.currentTimeMillis() - Long.parseLong(cooldownInfo[0])) / 1000L;

            // Check if players enderpearl cooldown is still active
            if (remaining > 0) {
                // Deny enderpearl
                event.setCancelled(true);

                // Send player a message
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', messageCooldown
                        .replace("{REMAINING}", DurationUtils.format(remaining))));
                return;
            }
        }

        // Give player an enderpearl cooldown
        enderpearlCooldowns.put(player.getName(), System.currentTimeMillis() + "-" + cooldown);
    }

}