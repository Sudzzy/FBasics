package org.originmc.fbasics.util;

import com.google.common.collect.ImmutableSet;
import org.bukkit.Material;

public final class MaterialUtils {

    private static final ImmutableSet<Integer> DOOR_BLOCKS = ImmutableSet.of(64, 71, 96, 167, 193, 194, 195, 196, 197);

    private static final ImmutableSet<Integer> FULL_BLOCKS = ImmutableSet.of(
            1, 2, 3, 4, 5, 7, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 35, 41, 42, 43, 45, 46, 47, 48,
            49, 52, 56, 57, 58, 61, 62, 73, 74, 79, 80, 82, 84, 86, 87, 89, 91, 95, 97, 98, 99, 100, 103, 110, 112, 121,
            125, 129, 133, 137, 152, 153, 155, 158, 159, 161, 162, 166, 168, 169, 170, 172, 173, 174, 179, 181
    );

    /**
     * Checks the ID of the material to see if it is considered a door block.
     *
     * @param material the material to check.
     * @return true if the material is considered a door block.
     */
    public static boolean isDoorBlock(Material material) {
        return DOOR_BLOCKS.contains(material.getId());
    }

    /**
     * Checks the ID of the material to see if it is considered a full block.
     *
     * @param material the material to check.
     * @return true if the material is considered a full block.
     */
    public static boolean isFullBlock(Material material) {
        return FULL_BLOCKS.contains(material.getId());
    }

}
