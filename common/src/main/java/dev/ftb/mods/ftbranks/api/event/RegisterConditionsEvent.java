package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankConditionFactory;

import java.util.function.BiConsumer;

public class RegisterConditionsEvent {
    private final BiConsumer<String, RankConditionFactory> consumer;

    public RegisterConditionsEvent(BiConsumer<String, RankConditionFactory> consumer) {
        this.consumer = consumer;
    }

    public void register(String id, RankConditionFactory factory) {
        consumer.accept(id, factory);
    }
}
