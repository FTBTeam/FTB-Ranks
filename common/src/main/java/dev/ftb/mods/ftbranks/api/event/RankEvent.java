package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    private final Rank rank;

    public RankEvent(RankManager manager, Rank rank) {
        this.manager = manager;
        this.rank = rank;
    }

    /**
     * Get the rank manager
     * @return the rank manager
     */
    @Nonnull
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
        private final GameProfile player;

        public Player(RankManager manager, Rank rank, GameProfile player) {
            super(manager, rank);
            this.player = player;
        }

        /**
         * Get the player's game profile. Not that the player is not necessarily online at this time.
         * @return the player's game profile
         */
        @Nonnull
        public GameProfile getPlayer() {
            return player;
        }
    }
}
