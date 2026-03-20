package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.PermissionValue;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Fired when the value of a permission node in a rank changes, via the {@code /ftbranks node} command.
 */
@FunctionalInterface
public interface PermissionNodeChangedEvent extends Consumer<PermissionNodeChangedEvent.Data> {
    record Data(RankManager manager, Rank rank, String node, @Nullable PermissionValue oldValue, @Nullable PermissionValue newValue) {
    }
}
