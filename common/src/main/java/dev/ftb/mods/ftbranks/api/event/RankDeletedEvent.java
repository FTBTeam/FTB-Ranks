package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;

/**
 * Fired when a rank is deleted, with the {@code /ftbranks delete} command.
 */
public class RankDeletedEvent extends RankEvent {
    public RankDeletedEvent(RankManagerImpl rankManager, Rank rank) {
        super(rankManager, rank);
    }
}
