package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

import java.util.function.Consumer;

public class RankEvent {
    public static final Event<Consumer<RanksReloadedEvent>> RELOADED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankCreatedEvent>> CREATED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankDeletedEvent>> DELETED = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankAddPlayerEvent>> ADD_PLAYER = EventFactory.createConsumerLoop();
    public static final Event<Consumer<RankRemovePlayerEvent>> REMOVE_PLAYER = EventFactory.createConsumerLoop();

    private RankManager manager;
    private final Rank rank;

    public RankEvent(RankManager manager, Rank rank) {
        this.manager = manager;
        this.rank = rank;
    }

    public RankManager getManager() {
        return manager;
    }

    public Rank getRank() {
        return rank;
    }

    public static class Player extends RankEvent {
        private final GameProfile player;

        public Player(RankManager manager, Rank rank, GameProfile player) {
            super(manager, rank);
            this.player = player;
        }

        public GameProfile getPlayer() {
            return player;
        }
    }
}
