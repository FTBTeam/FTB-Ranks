package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when a rank's condition changes, via the {@code /ftbranks condition} command.
 */
public class ConditionChangedEvent extends RankEvent {
    private final RankCondition oldCondition;
    private final RankCondition newCondition;

    public ConditionChangedEvent(RankManager manager, Rank rank, RankCondition oldCondition, RankCondition newCondition) {
        super(manager, rank);
        this.oldCondition = oldCondition;
        this.newCondition = newCondition;
    }

    /**
     * Get the previous condition
     * @return the previous condition
     */
    public RankCondition getOldCondition() {
        return oldCondition;
    }

    /**
     * Get the new condition
     * @return the new condition
     */
    public RankCondition getNewCondition() {
        return newCondition;
    }
}
