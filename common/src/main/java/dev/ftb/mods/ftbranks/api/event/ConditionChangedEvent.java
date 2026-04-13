package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankManager;

import java.util.function.Consumer;

/// Fired when a rank's condition changes, via the `/ftbranks condition` command.
@FunctionalInterface
public interface ConditionChangedEvent extends Consumer<ConditionChangedEvent.Data> {
    record Data(RankManager manager, Rank rank, RankCondition oldCondition, RankCondition newCondition) {}
}
