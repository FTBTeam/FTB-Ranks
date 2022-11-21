package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import javax.annotation.Nullable;

public class PermissionNodeChangedEvent extends RankEvent {
    private final String node;
    private final PermissionValue oldValue;
    private final PermissionValue newValue;

    public PermissionNodeChangedEvent(RankManager manager, Rank rank, String node, @Nullable PermissionValue oldValue, @Nullable PermissionValue newValue) {
        super(manager, rank);
        this.node = node;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getNode() {
        return node;
    }

    public PermissionValue getOldValue() {
        return oldValue;
    }

    public PermissionValue getNewValue() {
        return newValue;
    }
}
