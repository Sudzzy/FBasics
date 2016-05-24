package org.originmc.fbasics.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public class BukkitUtils {

    public static final String BUKKIT_VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    /**
     * Determines if the player is holding an item in either hand.
     *
     * @param player the player to test for.
     * @param material the material to test for.
     * @return true if player is holding this specific material in either hand.
     */
    public static boolean hasItemSelected(Player player, Material material) {
        if (BUKKIT_VERSION.compareTo("1.9") >= 0) {
            // Check items in both hands if server is using the 1.9 API or higher.
            ItemStack offHand = player.getInventory().getItemInOffHand();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (offHand != null && offHand.getType() == material) return true;
            if (mainHand != null && mainHand.getType() == material) return true;
        } else {
            // Only check item in main hand if server is using a lower API than 1.9.
            ItemStack hand = player.getItemInHand();
            if (hand != null && hand.getType() == material) return true;
        }

        return false;
    }

    /**
     * Determines if the player is holding one of a select number of items in
     * either hand.
     *
     * @param player the player to test for.
     * @param materials the materials to test for.
     * @return true if player is holding one of the select items.
     */
    public static boolean hasItemsSelected(Player player, Collection<Material> materials) {
        if (BUKKIT_VERSION.compareTo("1.9") >= 0) {
            // Check items in both hands if server is using the 1.9 API or higher.
            ItemStack offHand = player.getInventory().getItemInOffHand();
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (offHand != null && materials.contains(offHand.getType())) return true;
            if (mainHand != null && materials.contains(mainHand.getType())) return true;
        } else {
            // Only check item in main hand if server is using a lower API than 1.9.
            ItemStack hand = player.getItemInHand();
            if (hand != null && materials.contains(hand.getType())) return true;
        }

        return false;
    }
}
