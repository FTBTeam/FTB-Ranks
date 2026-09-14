package dev.ftb.mods.ftbranks.api.fabric;

import dev.ftb.mods.ftbranks.api.event.*;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Arrays;

public class FTBRanksEvents {
    public static final Event<RegisterConditionsEvent> REGISTER_CONDITIONS = EventFactory.createArrayBacked(RegisterConditionsEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<RanksReloadedEvent> RANK_RELOADED = EventFactory.createArrayBacked(RanksReloadedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<RankDeletedEvent> RANK_DELETED = EventFactory.createArrayBacked(RankDeletedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<RankCreatedEvent> RANK_CREATED = EventFactory.createArrayBacked(RankCreatedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<PlayerRemovedFromRankEvent> PLAYER_REMOVED_FROM_RANK = EventFactory.createArrayBacked(PlayerRemovedFromRankEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<PlayerAddedToRankEvent> PLAYER_ADDED_TO_RANK = EventFactory.createArrayBacked(PlayerAddedToRankEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<PermissionNodeChangedEvent> PERMISSION_NODE_CHANGED = EventFactory.createArrayBacked(PermissionNodeChangedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static final Event<ConditionChangedEvent> CONDITION_CHANGED = EventFactory.createArrayBacked(ConditionChangedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );
}
