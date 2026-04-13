package dev.ftb.mods.ftbranks.api.fabric;

import dev.ftb.mods.ftbranks.api.event.*;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

import java.util.Arrays;

public class FTBRanksEvents {
    public static Event<RegisterConditionsEvent> REGISTER_CONDITIONS = EventFactory.createArrayBacked(RegisterConditionsEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<RanksReloadedEvent> RANK_RELOADED = EventFactory.createArrayBacked(RanksReloadedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<RankDeletedEvent> RANK_DELETED = EventFactory.createArrayBacked(RankDeletedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<RankCreatedEvent> RANK_CREATED = EventFactory.createArrayBacked(RankCreatedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<PlayerRemovedFromRankEvent> PLAYER_REMOVED_FROM_RANK = EventFactory.createArrayBacked(PlayerRemovedFromRankEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<PlayerAddedToRankEvent> PLAYER_ADDED_TO_RANK = EventFactory.createArrayBacked(PlayerAddedToRankEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<PermissionNodeChangedEvent> PERMISSION_NODE_CHANGED = EventFactory.createArrayBacked(PermissionNodeChangedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );

    public static Event<ConditionChangedEvent> CONDITION_CHANGED = EventFactory.createArrayBacked(ConditionChangedEvent.class,
            callbacks -> data -> Arrays.stream(callbacks).forEach(c -> c.accept(data))
    );
}
