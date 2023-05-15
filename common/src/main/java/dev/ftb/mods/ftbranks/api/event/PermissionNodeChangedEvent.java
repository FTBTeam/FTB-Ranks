package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import javax.annotation.Nullable;

/**
 * Fired when the value of a permission node in a rank changes, via the {@code /ftbranks node} command.
 */
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

    /**
     * Get the node's old value
     * @return the old value
     */
    public PermissionValue getOldValue() {
        return oldValue;
    }

    /**
     * Get the node's new value
     * @return the new value
     */
    public PermissionValue getNewValue() {
        return newValue;
    }
}
