package org.originmc.fbasics.entity;

import com.google.gson.annotations.Expose;
import lombok.Data;
import org.originmc.fbasics.task.CommandWarmupTask;

@Data
public final class CommandModifier {

    private final String name;

    @Expose
    private long cooldown;

    private long warmup;

    private CommandWarmupTask task;

}
