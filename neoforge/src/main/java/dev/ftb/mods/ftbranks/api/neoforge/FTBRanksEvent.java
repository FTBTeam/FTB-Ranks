package dev.ftb.mods.ftbranks.api.neoforge;

import dev.ftb.mods.ftbranks.api.RankConditionFactory;
import dev.ftb.mods.ftbranks.api.event.*;
import net.neoforged.bus.api.Event;

public class FTBRanksEvent {
    public static class RegisterConditions extends Event {
        private final RegisterConditionsEvent.Data data;

        public RegisterConditions(RegisterConditionsEvent.Data data) {
            this.data = data;
        }

        public RegisterConditionsEvent.Data getData() {
            return data;
        }

        public void register(String name, RankConditionFactory factory) {
            data.consumer().accept(name, factory);
        }
    }

    public static class Reloaded extends Event {
        private final RanksReloadedEvent.Data data;

        public Reloaded(RanksReloadedEvent.Data data) {
            this.data = data;
        }

        public RanksReloadedEvent.Data getData() {
            return data;
        }
    }

    public static class Deleted extends Event {
        private final RankDeletedEvent.Data data;

        public Deleted(RankDeletedEvent.Data data) {
            this.data = data;
        }

        public RankDeletedEvent.Data getData() {
            return data;
        }
    }

    public static class Created extends Event {
        private final RankCreatedEvent.Data data;

        public Created(RankCreatedEvent.Data data) {
            this.data = data;
        }

        public RankCreatedEvent.Data getData() {
            return data;
        }
    }

    public static class PlayerRemoved extends Event {
        private final PlayerRemovedFromRankEvent.Data data;

        public PlayerRemoved(PlayerRemovedFromRankEvent.Data data) {
            this.data = data;
        }

        public PlayerRemovedFromRankEvent.Data getData() {
            return data;
        }
    }

    public static class NodeChanged extends Event {
        private final PermissionNodeChangedEvent.Data data;

        public NodeChanged(PermissionNodeChangedEvent.Data data) {
            this.data = data;
        }

        public PermissionNodeChangedEvent.Data getData() {
            return data;
        }
    }

    public static class ConditionChanged extends Event {
        private final ConditionChangedEvent.Data data;

        public ConditionChanged(ConditionChangedEvent.Data data) {
            this.data = data;
        }

        public ConditionChangedEvent.Data getData() {
            return data;
        }
    }
}
