package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import java.util.function.Consumer;

/**
 * Fired when a rank is deleted, with the {@code /ftbranks delete} command.
 */
@FunctionalInterface
public interface RankDeletedEvent extends Consumer<RankDeletedEvent.Data> {
    record Data(RankManager manager, Rank rank) {}
}
