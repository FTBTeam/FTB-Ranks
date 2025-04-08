package dev.ftb.mods.ftbranks;

import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftblibrary.util.PlayerDisplayNameUtil;
import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import dev.ftb.mods.ftbranks.impl.decorate.TextComponentParser;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerNameFormatting {
    public static Component formatPlayerName(Player player, Component originalName) {
        if (!(player instanceof ServerPlayer serverPlayer)) return originalName;

        String format = FTBRanksAPI.getPermissionValue(serverPlayer, "ftbranks.name_format").asString().orElse("");

        if (!format.isEmpty()) {
            try {
                return TextComponentParser.parse(format, s -> s.equals("name") ? originalName : null);
            } catch (Exception ex) {
                String s = "Error parsing " + format + ": " + ex;
                FTBRanks.LOGGER.error(s);
                return Component.literal("BrokenFormatting").withStyle(Style.EMPTY
                        .withColor(ChatFormatting.RED)
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal(s)))
                );
            }
        } else {
            return originalName;
        }
    }

    public static void refreshPlayerNames() {
        MinecraftServer server = GameInstance.getServer();
        if (server != null) {
            server.getPlayerList().getPlayers().forEach(PlayerDisplayNameUtil::refreshDisplayName);
        }
    }
}
