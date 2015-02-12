package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.originmc.fbasics.FBasics;

import java.util.*;

public class ChestDupeListener implements Listener {

    private static final BlockFace[] BLOCK_FACES = {
            BlockFace.NORTH,
            BlockFace.EAST,
            BlockFace.SOUTH,
            BlockFace.WEST
    };
    private final String message;
    private final List<Material> blocks = new ArrayList<Material>();
    private final List<Material> doubleBlocks = new ArrayList<Material>();
    private final List<EntityType> entities = new ArrayList<EntityType>();
    private final Map<UUID, List<Location>> openBlocks = new HashMap<UUID, List<Location>>();
    private final Map<UUID, Entity> openEntities = new HashMap<UUID, Entity>();

    public ChestDupeListener(FBasics plugin) {
        FileConfiguration config = plugin.getConfig();
        FileConfiguration materials = plugin.getMaterials();
        FileConfiguration language = plugin.getLanguage();
        String error = language.getString("general.error.prefix");

        this.message = error + language.getString("patcher.error.chest-dupe");

        for (String block : materials.getStringList("inventory-blocks")) {
            this.blocks.add(Material.getMaterial(block));
        }

        for (String doubleBlock : materials.getStringList("inventory-double-blocks")) {
            this.doubleBlocks.add(Material.getMaterial(doubleBlock));
        }

        for (String entity : materials.getStringList("inventory-entities")) {
            this.entities.add(EntityType.valueOf(entity));
        }

        if (config.getBoolean("patcher.chest-dupe")) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            plugin.getLogger().info("Chest-Dupe module loaded");
        }
    }

    private boolean isProtected(Location location) {
        for (List<Location> protectedLocations : this.openBlocks.values()) {
            if (protectedLocations.contains(location)) {
                return true;
            }
        }
        return false;
    }

    private List<Location> getNearBlockLocations(Block block) {
        List<Location> nearBlockLocations = new ArrayList<Location>();
        Material material = block.getType();
        nearBlockLocations.add(block.getLocation());

        if (this.doubleBlocks.contains(material)) {
            return nearBlockLocations;
        }

        for (BlockFace blockFace : BLOCK_FACES) {
            Block nearBlock = block.getRelative(blockFace);
            if (nearBlock.getType().equals(material)) {
                nearBlockLocations.add(nearBlock.getLocation());
            }
        }

        return nearBlockLocations;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        HumanEntity entity = event.getPlayer();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        UUID uuid = player.getUniqueId();

        try {
            @SuppressWarnings("deprecation")
            Block block = player.getTargetBlock(null, 6);
            Material blockType = block.getType();

            for (Material inventoryBlock : this.blocks) {
                if (blockType.equals(inventoryBlock)) {
                    List<Location> locations = getNearBlockLocations(block);
                    this.openBlocks.put(uuid, locations);
                    return;
                }
            }
        } catch (Exception exception) {
            // Continue //
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!player.isSneaking()) {
            return;
        }

        Entity entity = event.getRightClicked();
        EntityType entityType = entity.getType();

        if (entityType.equals(EntityType.HORSE) && this.entities.contains(EntityType.HORSE)) {
            Horse horse = (Horse) entity;

            if (!horse.isTamed()) {
                return;
            }

            this.openEntities.put(uuid, entity);
            return;
        }

        for (EntityType inventoryEntity : this.entities) {
            if (entityType.equals(inventoryEntity)) {
                this.openEntities.put(uuid, entity);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity entity = event.getPlayer();

        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        UUID uuid = player.getUniqueId();

        this.openBlocks.remove(uuid);
        this.openEntities.remove(uuid);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isProtected(event.getBlock().getLocation())) {
            return;
        }

        event.setCancelled(true);
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damaged = event.getEntity();

        if (!(damager instanceof Player) || !(this.openEntities.containsValue(damaged))) {
            return;
        }

        Player player = (Player) damager;

        event.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        Entity damager = event.getAttacker();
        Entity damaged = event.getVehicle();

        if (!(damager instanceof Player) || !(this.openEntities.containsValue(damaged))) {
            return;
        }

        Player player = (Player) damager;

        event.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        List<Block> destroyed = event.blockList();
        Iterator<Block> iterator = destroyed.iterator();

        while (iterator.hasNext()) {
            Block block = iterator.next();
            if (isProtected(block.getLocation())) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (this.openEntities.values().contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
