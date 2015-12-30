package org.originmc.fbasics.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.util.DurationUtils;

public final class EnderpearlCooldownPlaceholder implements PlaceholderReplacer {

    private final FBasics plugin;

    public EnderpearlCooldownPlaceholder(FBasics plugin) {
        this.plugin = plugin;
        PlaceholderAPI.registerPlaceholder(plugin, "fbasics_enderpearl_cooldown", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        // Get remaining enderpearl cooldown for the player.
        long remaining = 0;
        if (event.getPlayer() != null) {
            User user = plugin.getOrCreateUser(event.getPlayer().getUniqueId());
            remaining = DurationUtils.calculateRemaining(user.getEnderpearlCooldown());
        }

        // Return the cooldown in text format.
        return DurationUtils.format(remaining);
    }

}
