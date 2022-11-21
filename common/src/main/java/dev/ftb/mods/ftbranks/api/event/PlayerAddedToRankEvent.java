package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

public class PlayerAddedToRankEvent extends RankEvent.Player {
    public PlayerAddedToRankEvent(RankManager manager, Rank rank, GameProfile player) {
        super(manager, rank, player);
    }
}
