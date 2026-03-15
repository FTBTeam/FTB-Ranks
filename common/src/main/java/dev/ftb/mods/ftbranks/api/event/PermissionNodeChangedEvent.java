package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import org.jspecify.annotations.Nullable;

/**
 * Fired when the value of a permission node in a rank changes, via the {@code /ftbranks node} command.
 */
@FunctionalInterface
public interface PermissionNodeChangedEvent {
    void onPermissionNodeChanged(Data data);

    record Data(RankManager manager, Rank rank, String node, @Nullable PermissionValue oldValue, @Nullable PermissionValue newValue) {}
}
