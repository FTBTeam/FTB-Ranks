package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;

public class RankCreatedEvent extends RankEvent {
    public RankCreatedEvent(RankManagerImpl rankManager, Rank rank) {
        super(rankManager, rank);
    }
}
