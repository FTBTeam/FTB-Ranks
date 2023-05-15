package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

/**
 * Fired when a player is added to a rank, with the {@code /ftbranks add} command.
 */
public class PlayerAddedToRankEvent extends RankEvent.Player {
    public PlayerAddedToRankEvent(RankManager manager, Rank rank, GameProfile player) {
        super(manager, rank, player);
    }
}
