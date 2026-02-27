package dev.ftb.mods.ftbranks.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import net.minecraft.server.players.NameAndId;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Common superclass for all rank-related events
 */
public class RankEvent {
    public static final Event<Consumer<RanksReloadedEvent>> RELOADED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankCreatedEvent>> CREATED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankDeletedEvent>> DELETED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<PlayerAddedToRankEvent>> ADD_PLAYER = EventFactory.createConsumerLoop();
    public static final Event<Consumer<PlayerRemovedFromRankEvent>> REMOVE_PLAYER = EventFactory.createConsumerLoop();
    public static final Event<Consumer<PermissionNodeChangedEvent>> PERMISSION_CHANGED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<ConditionChangedEvent>> CONDITION_CHANGED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RegisterConditionsEvent>> REGISTER_CONDITIONS = EventFactory.createConsumerLoop();

    private final RankManager manager;
    @Nullable
    private final Rank rank;

    public RankEvent(RankManager manager, @Nullable Rank rank) {
        this.manager = manager;
        this.rank = rank;
    }

    /**
     * Get the rank manager
     * @return the rank manager
     */
    public RankManager getManager() {
        return manager;
    }

    /**
     * Get the rank to which this event refers
     * @return the rank
     */
    @Nullable
    public Rank getRank() {
        return rank;
    }

    /**
     * Common superclass for rank events with a player involved
     */
    public static class Player extends RankEvent {
        private final NameAndId player;

        public Player(RankManager manager, Rank rank, NameAndId player) {
            super(manager, rank);
            this.player = player;
        }

        /**
         * Get the player's name and ID. Note that the player is not necessarily online at this time.
         * @return the player's name and ID
         */
        public NameAndId getPlayer() {
            return player;
        }
    }
}
