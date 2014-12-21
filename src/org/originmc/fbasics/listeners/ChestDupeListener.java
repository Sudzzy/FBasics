package org.originmc.fbasics.listeners;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDispenseEvent;
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

    private final String message;
    private final List<Material> blocks = new ArrayList<Material>();
    private final List<Material> doubleBlocks = new ArrayList<Material>();
    private final List<EntityType> entities = new ArrayList<EntityType>();
    private Map<Player, List<Block>> openBlocks = new HashMap<Player, List<Block>>();
    private Map<Player, Entity> openEntities = new HashMap<Player, Entity>();

    public ChestDupeListener(FBasics plugin) {
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
    }

    private boolean isProtected(Block block) {
        for (List<Block> blocks : this.openBlocks.values()) {
            if (blocks.contains(block)) {
                return true;
            }
        }
        return false;
    }

    private List<Block> getSurroundingBlocks(Block block) {
        List<Block> blocks = new ArrayList<Block>();
        Material material = block.getType();
        blocks.add(block);

        if (!this.blocks.contains(material)) {
            return blocks;
        }

        for (Material doubleBlock : this.doubleBlocks) {

            if (material.equals(doubleBlock)) {

                List<Block> opposingBlocks = new ArrayList<Block>();
                opposingBlocks.add(block.getRelative(BlockFace.NORTH));
                opposingBlocks.add(block.getRelative(BlockFace.EAST));
                opposingBlocks.add(block.getRelative(BlockFace.SOUTH));
                opposingBlocks.add(block.getRelative(BlockFace.WEST));

                for (Block opposingBlock : opposingBlocks) {
                    if (opposingBlock.getType().equals(material)) {
                        blocks.add(opposingBlock);
                        return blocks;
                    }
                }

                return blocks;
            }
        }
        return blocks;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent e) {
        HumanEntity entity = e.getPlayer();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        @SuppressWarnings("deprecation")
        Block block = player.getTargetBlock(null, 6);
        Material blockType = block.getType();

        for (Material inventoryBlock : this.blocks) {
            if (blockType.equals(inventoryBlock)) {
                List<Block> blocks = getSurroundingBlocks(block);
                this.openBlocks.put(player, blocks);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Entity entity = e.getRightClicked();
        EntityType entityType = entity.getType();
        Player player = e.getPlayer();

        if (entityType.equals(EntityType.HORSE) && this.entities.contains(EntityType.HORSE)) {
            if (!player.isSneaking()) return;

            Horse horse = (Horse) entity;

            if (!horse.isTamed()) return;

            this.openEntities.put(player, entity);
            return;
        }

        for (EntityType inventoryEntity : this.entities) {
            if (entityType.equals(inventoryEntity)) {
                this.openEntities.put(player, entity);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        HumanEntity entity = e.getPlayer();

        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;
        if (this.openBlocks.containsKey(player)) {
            this.openBlocks.remove(player);
        }
        this.openEntities.remove(player);
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Block block = e.getBlock();

        if (!isProtected(block)) return;

        Player player = e.getPlayer();

        e.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onEntityHit(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        Entity damaged = e.getEntity();

        if (!(damager instanceof Player)) return;
        if (!(this.openEntities.containsValue(damaged))) return;

        Player player = (Player) damager;

        e.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onVehicleHit(VehicleDamageEvent e) {
        Entity damager = e.getAttacker();
        Entity damaged = e.getVehicle();

        if (!(damager instanceof Player)) return;
        if (!(this.openEntities.containsValue(damaged))) return;

        Player player = (Player) damager;

        e.setCancelled(true);
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', this.message));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        List<Block> destroyed = e.blockList();
        Iterator<Block> it = destroyed.iterator();

        while (it.hasNext()) {
            Block block = it.next();
            if (isProtected(block)) it.remove();
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (this.openEntities.values().contains(event.getEntity())) event.setCancelled(true);
    }
}
