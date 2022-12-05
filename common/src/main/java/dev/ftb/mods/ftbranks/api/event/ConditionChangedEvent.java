package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankManager;

public class ConditionChangedEvent extends RankEvent {
    private final RankCondition oldCondition;
    private final RankCondition newCondition;

    public ConditionChangedEvent(RankManager manager, Rank rank, RankCondition oldCondition, RankCondition newCondition) {
        super(manager, rank);
        this.oldCondition = oldCondition;
        this.newCondition = newCondition;
    }

    public RankCondition getOldCondition() {
        return oldCondition;
    }

    public RankCondition getNewCondition() {
        return newCondition;
    }
}
