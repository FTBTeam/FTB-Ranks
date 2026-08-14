package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.io.IOException;

public class RefreshReadmeCommand {
    static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("refresh_readme")
                .executes(context -> refreshReadme(context.getSource()));
    }

    private static int refreshReadme(CommandSourceStack source) {
        try {
            ((RankManagerImpl) FTBRanksAPI.manager()).refreshReadme();
            source.sendSuccess(() -> Component.literal("README file refreshed!"), false);
            return 1;
        } catch (IOException ex) {
            source.sendFailure(Component.literal("Failed to refresh README file: " + ex.getLocalizedMessage()));
            return 0;
        }
    }
}
