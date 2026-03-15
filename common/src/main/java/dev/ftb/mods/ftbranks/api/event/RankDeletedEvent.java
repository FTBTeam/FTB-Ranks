package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when a rank is deleted, with the {@code /ftbranks delete} command.
 */
@FunctionalInterface
public interface RankDeletedEvent {
    void onRankDeleted(Data data);

    record Data(RankManager manager, Rank rank) {}
}
