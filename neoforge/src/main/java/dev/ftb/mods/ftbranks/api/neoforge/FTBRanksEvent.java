package dev.ftb.mods.ftbranks.api.neoforge;

import dev.ftb.mods.ftblibrary.api.neoforge.BaseEventWithData;
import dev.ftb.mods.ftbranks.api.RankConditionFactory;
import dev.ftb.mods.ftbranks.api.event.*;

public class FTBRanksEvent {
    public static class RegisterConditions extends BaseEventWithData<RegisterConditionsEvent.Data> {
        public RegisterConditions(RegisterConditionsEvent.Data data) {
            super(data);
        }

        public void register(String name, RankConditionFactory factory) {
            data.consumer().accept(name, factory);
        }
    }

    public static class Reloaded extends BaseEventWithData<RanksReloadedEvent.Data> {
        public Reloaded(RanksReloadedEvent.Data data) {
            super(data);
        }
    }

    public static class Deleted extends BaseEventWithData<RankDeletedEvent.Data> {
        public Deleted(RankDeletedEvent.Data data) {
            super(data);
        }
    }

    public static class Created extends BaseEventWithData<RankCreatedEvent.Data> {
        public Created(RankCreatedEvent.Data data) {
            super(data);
        }
    }

    public static class PlayerAdded extends BaseEventWithData<PlayerAddedToRankEvent.Data> {
        public PlayerAdded(PlayerAddedToRankEvent.Data data) {
            super(data);
        }
    }

    public static class PlayerRemoved extends BaseEventWithData<PlayerRemovedFromRankEvent.Data> {
        public PlayerRemoved(PlayerRemovedFromRankEvent.Data data) {
            super(data);
        }
    }

    public static class PermissionNodeChanged extends BaseEventWithData<PermissionNodeChangedEvent.Data> {
        public PermissionNodeChanged(PermissionNodeChangedEvent.Data data) {
            super(data);
        }
    }

    public static class ConditionChanged extends BaseEventWithData<ConditionChangedEvent.Data> {
        public ConditionChanged(ConditionChangedEvent.Data data) {
            super(data);
        }
    }
}
