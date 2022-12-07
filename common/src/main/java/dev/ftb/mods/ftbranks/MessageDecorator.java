package dev.ftb.mods.ftbranks;

import dev.ftb.mods.ftbranks.api.FTBRanksAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class MessageDecorator {
    /**
     * Common method called by Forge and Fabric-specific chat decoration event handlers. Note: only message text is
     * decorated here; sender name decoration is done via modifying the return value of player.getDisplayName(), via
     * Forge event or Fabric mixin.
     *
     * @param player player sending the message
     * @param text the mutable message text (to be mutated in-place if necessary)
     * @return true if decoration was applied, false if not
     */
    public static boolean decorateMessage(ServerPlayer player, MutableComponent text) {
        MutableBoolean changed = new MutableBoolean(false);

        ChatFormatting color = ChatFormatting.getByName(FTBRanksAPI.getPermissionValue(player, "ftbranks.chat_text.color").asString().orElse(null));
        if (color != null) {
            text.setStyle(text.getStyle().applyFormat(color));
            changed.setTrue();
        }

        addStyle(player, text, "ftbranks.chat_text.bold", ChatFormatting.BOLD, changed);
        addStyle(player, text, "ftbranks.chat_text.italic", ChatFormatting.ITALIC, changed);
        addStyle(player, text, "ftbranks.chat_text.underlined", ChatFormatting.UNDERLINE, changed);
        addStyle(player, text, "ftbranks.chat_text.strikethrough", ChatFormatting.STRIKETHROUGH, changed);
        addStyle(player, text, "ftbranks.chat_text.obfuscated", ChatFormatting.OBFUSCATED, changed);

        return changed.booleanValue();
    }

    private static void addStyle(ServerPlayer player, MutableComponent component, String node, ChatFormatting modifier, MutableBoolean changed) {
        if (FTBRanksAPI.getPermissionValue(player, node).asBooleanOrFalse()) {
            component.setStyle(component.getStyle().applyFormat(modifier));
            changed.setTrue();
        }
    }
}
