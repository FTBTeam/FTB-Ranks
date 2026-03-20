package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankManager;

import java.util.function.Consumer;

/**
 * Fired when ranks are loaded from disk, either on server startup, or when {@code /ranks reload} is run.
 */
@FunctionalInterface
public interface RanksReloadedEvent extends Consumer<RanksReloadedEvent.Data> {
    record Data(RankManager manager) {}
}
