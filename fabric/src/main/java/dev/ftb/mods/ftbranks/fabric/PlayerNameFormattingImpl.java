package dev.ftb.mods.ftbranks.fabric;

import dev.architectury.utils.GameInstance;
import dev.ftb.mods.ftbranks.PlayerDisplayNameCache;

public class PlayerNameFormattingImpl {
    public static void refreshPlayerNames() {
        GameInstance.getServer().getPlayerList().getPlayers().forEach(p -> ((PlayerDisplayNameCache) p).clearCachedDisplayName());
    }
}
