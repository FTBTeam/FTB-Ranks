package dev.ftb.mods.ftbranks;

import com.google.common.base.Suppliers;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.DummyRank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RankArgumentType implements ArgumentType<Rank> {
    private final Supplier<Set<String>> knownRanks;

    private RankArgumentType() {
        knownRanks = Suppliers.memoize(() -> FTBRanksAPI.INSTANCE.getManager().getAllRanks().stream().map(Rank::getId).collect(Collectors.toSet()));
    }

    private RankArgumentType(Collection<String> known) {
        knownRanks = Suppliers.ofInstance(Set.copyOf(known));
    }

    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> new TranslatableComponent("ftbranks.unknown_rank", new TextComponent(object.toString())));

    public static RankArgumentType rank() {
        return new RankArgumentType();
    }

    public static Rank getRank(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Rank.class);
    }

    @Override
    public Rank parse(StringReader reader) throws CommandSyntaxException {
        String rankId = reader.readUnquotedString();
        return FTBRanksAPI.INSTANCE.getManager() == null ?
                new DummyRank(rankId) :  // client side
                FTBRanksAPI.INSTANCE.getManager().getRank(rankId).orElseThrow(() -> ERROR_INVALID_VALUE.create(rankId));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(knownRanks.get(), builder);
    }

    public static class Serializer implements ArgumentSerializer<RankArgumentType> {
        @Override
        public void serializeToNetwork(RankArgumentType argumentType, FriendlyByteBuf buf) {
            buf.writeVarInt(argumentType.knownRanks.get().size());
            argumentType.knownRanks.get().forEach(buf::writeUtf);
        }

        @Override
        public RankArgumentType deserializeFromNetwork(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Set<String> known = new HashSet<>();
            for (int i = 0; i < size; i++) {
                known.add(buf.readUtf());
            }
            return new RankArgumentType(known);
        }

        @Override
        public void serializeToJson(RankArgumentType argumentType, JsonObject json) {
            JsonArray array = new JsonArray(argumentType.knownRanks.get().size());
            argumentType.knownRanks.get().forEach(array::add);
            json.add("ranks", array);
        }
    }
}
