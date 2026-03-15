package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when ranks are loaded from disk, either on server startup, or when {@code /ranks reload} is run.
 */
@FunctionalInterface
public interface RanksReloadedEvent {
    void onReload(Data data);

    record Data(RankManager manager) {}
}
