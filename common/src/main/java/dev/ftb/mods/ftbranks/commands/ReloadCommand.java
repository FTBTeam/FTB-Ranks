package dev.ftb.mods.ftbranks.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbranks.impl.FTBRanksAPIImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Objects;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return literal("reload")
                .executes(c -> reloadRanks(c.getSource())
                );
    }

    private static int reloadRanks(CommandSourceStack source) {
        try {
            Objects.requireNonNull(FTBRanksAPIImpl.manager).reload();
            source.sendSuccess(() -> Component.literal("Ranks reloaded from disk!"), true);

            for (ServerPlayer p : source.getServer().getPlayerList().getPlayers()) {
                source.getServer().getPlayerList().sendPlayerPermissionLevel(p);
            }

            return 1;
        } catch (Exception ex) {
            // Unlike with server startup, reload errors are not fatal.
            // We let the server continue with the previous good ranks configuration.
            ex.printStackTrace();
            source.sendFailure(Component.literal("could not reload ranks:" + ex.getLocalizedMessage()));
            return 0;
        }
    }
}
