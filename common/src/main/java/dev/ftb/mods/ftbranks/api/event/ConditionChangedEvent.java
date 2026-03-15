package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when a rank's condition changes, via the {@code /ftbranks condition} command.
 */
@FunctionalInterface
public interface ConditionChangedEvent {
    void onConditionChange(Data data);

    record Data(RankManager manager, Rank rank, RankCondition oldCondition, RankCondition newCondition) {}
}
