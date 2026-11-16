package dev.ftb.mods.ftbranks.api.event;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankConditionFactory;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/// Fired during server startup to allow registration of custom rank conditions. You can register your own
/// conditions using this event.
@FunctionalInterface
public interface RegisterConditionsEvent extends Consumer<RegisterConditionsEvent.Data> {
    record Data(BiConsumer<String, RankConditionFactory> consumer) {
        /// Register a custom condition factory object. Use this method if your condition has serialized JSON
        /// data, and requires a rank to be passed to its constructor.
        ///
        /// @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. `"yourmod:newrank"`
        /// @param factory the factory object for creating instances of the condition
        public void register(String id, RankConditionFactory factory) {
            consumer.accept(id, factory);
        }

        /// Register a custom condition factory object. Use this method if your condition has serialized JSON data
        /// but does not require a rank to be passed to its constructor.
        ///
        /// @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. `"yourmod:newrank"`
        /// @param factory the factory object for creating instances of the condition
        public void register(String id, Function<Json5Object, RankCondition> factory) {
            consumer.accept(id, (_, json) -> factory.apply(json));
        }

        /// Register a supplier for a simple condition, which does not need a rank or serialized JSON data to create.
        ///
        /// @param id the rank's unique id; it's recommended to prefix this with your modid, e.g. `"yourmod:newrank"`
        /// @param supplier a supplier for the condition object
        public void registerSimple(String id, Supplier<RankCondition> supplier) {
            consumer.accept(id, (_, _) -> supplier.get());
        }
    }
}
