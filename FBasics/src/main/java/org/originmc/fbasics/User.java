package org.originmc.fbasics;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.EnderPearl;
import org.bukkit.event.EventPriority;
import org.originmc.fbasics.task.WildernessTask;

import java.lang.ref.WeakReference;
import java.util.HashMap;

@Data
public final class User {

    @Expose
    private final HashMap<String, CommandModifier> modifiers = new HashMap<>();

    private long looterMessageCooldown;

    private long enderpearlCooldown;

    private long enderpearlDoorCooldown;

    private boolean teleported;

    private Location validLocation;

    private WildernessTask wildernessTask;

    private WeakReference<EnderPearl> pearl;

    /**
     * Checks to see if the user currently has a thrown enderpearl.
     *
     * @return true if the player has a thrown enderpearl.
     */
    public boolean isThrowingPearl() {
        // User is not throwing an enderpearl if not set.
        if (pearl == null) return false;

        // User is throwing an enderpearl if the pearl is not null or dead.
        EnderPearl pearl = this.pearl.get();
        return pearl != null && !pearl.isDead();
    }

}
