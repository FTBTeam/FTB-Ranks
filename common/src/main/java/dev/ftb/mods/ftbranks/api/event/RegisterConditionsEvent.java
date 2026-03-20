package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankConditionFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Fired during server startup to allow registration of custom rank conditions. You can register your own
/// conditions using this event.
@FunctionalInterface
public interface RegisterConditionsEvent extends Consumer<RegisterConditionsEvent.Data> {
    record Data(BiConsumer<String, RankConditionFactory> consumer) {
        /// Register a custom condition factory object.
        ///
        /// @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. `"yourmod:newrank"`
        /// @param factory the factory object for creating instances of the condition
        public void register(String id, RankConditionFactory factory) {
            consumer.accept(id, factory);
        }

        /// Register a supplier for a simple condition, which does not need a rank or serialized JSON data to create.
        ///
        /// @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. `"yourmod:newrank"`
        /// @param supplier a supplier for the condition object
        public void registerSimple(String id, Supplier<RankCondition> supplier) {
            consumer.accept(id, (ignoredRank, ignoredJson) -> supplier.get());
        }
    }
}
