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

    public boolean isThrowingPearl() {
        EnderPearl pearl = this.pearl.get();
        return pearl != null && !pearl.isDead();
    }

}
