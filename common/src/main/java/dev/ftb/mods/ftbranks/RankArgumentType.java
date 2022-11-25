package dev.ftb.mods.ftbranks;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RankArgumentType implements ArgumentType<Rank> {
    public static final DynamicCommandExceptionType ERROR_INVALID_VALUE = new DynamicCommandExceptionType((object) -> Component.translatable("ftbranks.unknown_rank", Component.literal(object.toString())));

    public static RankArgumentType rank() {
        return new RankArgumentType();
    }

    public static Rank getRank(CommandContext<CommandSourceStack> commandContext, String string) {
        return commandContext.getArgument(string, Rank.class);
    }

    @Override
    public Rank parse(StringReader reader) throws CommandSyntaxException {
        String string = reader.readUnquotedString();
        return FTBRanksAPI.INSTANCE.getManager().getRank(string).orElseThrow(() -> ERROR_INVALID_VALUE.create(string));
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> names = FTBRanksAPI.INSTANCE.getManager().getAllRanks().stream().map(Rank::getId).toList();
        return SharedSuggestionProvider.suggest(names, builder);
    }
}
