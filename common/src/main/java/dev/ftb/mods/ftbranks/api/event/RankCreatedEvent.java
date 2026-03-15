package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;

/**
 * Fired when a rank is created, with the {@code /ftbranks delete} command.
 */
@FunctionalInterface
public interface RankCreatedEvent {
    void onRankCreated(Data data);

    record Data(RankManager manager, Rank rank) {}
}
