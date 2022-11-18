package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;

public class RankAddPlayerEvent extends RankEvent.Player {
    public RankAddPlayerEvent(RankManager manager, Rank rank, GameProfile player) {
        super(manager, rank, player);
    }
}
