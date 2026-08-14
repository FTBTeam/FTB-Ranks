package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import java.util.function.Consumer;

/// Fired when a rank is created, with the `/ftbranks create` command.
@FunctionalInterface
public interface RankCreatedEvent extends Consumer<RankCreatedEvent.Data> {
    record Data(RankManager manager, Rank rank) {}
}
