package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankManager;

public class RanksReloadedEvent extends RankEvent {
    public RanksReloadedEvent(RankManager manager) {
        super(manager, null);
    }
}
