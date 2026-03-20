package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;

import java.util.function.Consumer;

/**
 * Fired when a rank is created, with the {@code /ftbranks delete} command.
 */
@FunctionalInterface
public interface RankCreatedEvent extends Consumer<RankCreatedEvent.Data> {
    record Data(RankManager manager, Rank rank) {}
}
