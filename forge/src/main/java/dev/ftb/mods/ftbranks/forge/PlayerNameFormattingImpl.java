package dev.ftb.mods.ftbranks.forge;

import dev.architectury.utils.GameInstance;
import net.minecraft.world.entity.player.Player;

public class PlayerNameFormattingImpl {
    public static void refreshPlayerNames() {
        GameInstance.getServer().getPlayerList().getPlayers().forEach(Player::refreshDisplayName);
    }
}
