package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankConditionFactory;

import java.util.function.BiConsumer;

/**
 * Fired during server startup to allow registration of custom rank conditions. You can register your own
 * conditions using this event.
 */
public class RegisterConditionsEvent {
    private final BiConsumer<String, RankConditionFactory> consumer;

    public RegisterConditionsEvent(BiConsumer<String, RankConditionFactory> consumer) {
        this.consumer = consumer;
    }

    /**
     * Register a custom condition factory object.
     * @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. {@code "yourmod:newrank"}
     * @param factory the factory object for creating instances of the condition
     */
    public void register(String id, RankConditionFactory factory) {
        consumer.accept(id, factory);
    }
}
