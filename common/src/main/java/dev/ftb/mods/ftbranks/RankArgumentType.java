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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RankArgumentType implements ArgumentType<Rank> {
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("ftbranks.unknown_rank", Component.literal(object.toString())));
    private final Supplier<Set<String>> knownRanks;

    private RankArgumentType() {
        knownRanks = () -> FTBRanksAPI.INSTANCE.getManager().getAllRanks().stream().map(Rank::getId).collect(Collectors.toSet());
    }

    private RankArgumentType(Collection<String> known) {
        knownRanks = Suppliers.ofInstance(Set.copyOf(known));
    }

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

    public static class Info implements ArgumentTypeInfo<RankArgumentType, Info.Template> {
        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
            buf.writeVarInt(template.knownRanks.size());
            template.knownRanks.forEach(buf::writeUtf);
        }

        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf buf) {
            int size = buf.readVarInt();
            Set<String> known = new HashSet<>();
            for (int i = 0; i < size; i++) {
                known.add(buf.readUtf());
            }
            return new Template(known);
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            JsonArray array = new JsonArray(template.knownRanks.size());
            template.knownRanks.forEach(array::add);
            json.add("ranks", array);
        }

        @Override
        public Template unpack(RankArgumentType argumentType) {
            return new Template(argumentType.knownRanks.get());
        }

        private class Template implements ArgumentTypeInfo.Template<RankArgumentType> {
            final Set<String> knownRanks;

            Template(Set<String> knownRanks) {
                this.knownRanks = knownRanks;
            }

            @Override
            public RankArgumentType instantiate(CommandBuildContext commandBuildContext) {
                return new RankArgumentType(knownRanks);
            }

            @Override
            public ArgumentTypeInfo<RankArgumentType, ?> type() {
                return Info.this;
            }
        }
    }
}
