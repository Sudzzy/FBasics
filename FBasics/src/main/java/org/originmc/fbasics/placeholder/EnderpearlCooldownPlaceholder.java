package org.originmc.fbasics.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import be.maximvdw.placeholderapi.PlaceholderReplaceEvent;
import be.maximvdw.placeholderapi.PlaceholderReplacer;
import org.originmc.fbasics.FBasics;
import org.originmc.fbasics.entity.User;
import org.originmc.fbasics.util.DurationUtils;

import java.text.DecimalFormat;

public final class EnderpearlCooldownPlaceholder implements PlaceholderReplacer {

    private final DecimalFormat timeFormat = new DecimalFormat("#0.0");

    private final FBasics plugin;

    public EnderpearlCooldownPlaceholder(FBasics plugin) {
        this.plugin = plugin;
        PlaceholderAPI.registerPlaceholder(plugin, "fbasics_enderpearl_cooldown", this);
    }

    @Override
    public String onPlaceholderReplace(PlaceholderReplaceEvent event) {
        // Get remaining enderpearl cooldown for the player.
        double remaining = 0;
        if (event.getPlayer() != null) {
            User user = plugin.getOrCreateUser(event.getPlayer().getUniqueId());
            remaining = ((double) DurationUtils.calculateRemaining(user.getEnderpearlCooldown())) / 1000;
        }

        // Return inactive if there is no cooldown remaining.
        if (remaining < 0) {
            return "Inactive";
        }

        // Return the cooldown in a compact time format.
        return timeFormat.format(remaining);
    }

}
