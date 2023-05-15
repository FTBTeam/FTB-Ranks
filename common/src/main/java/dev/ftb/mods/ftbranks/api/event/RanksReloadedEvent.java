package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when ranks are loaded from disk, either on server startup, or when {@code /ranks reload} is run.
 */
public class RanksReloadedEvent extends RankEvent {
    public RanksReloadedEvent(RankManager manager) {
        super(manager, null);
    }
}
