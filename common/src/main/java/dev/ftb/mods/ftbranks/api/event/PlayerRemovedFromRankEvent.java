package dev.ftb.mods.ftbranks.api.event;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;

public class PlayerRemovedFromRankEvent extends RankEvent.Player {
    public PlayerRemovedFromRankEvent(RankManagerImpl manager, Rank rank, GameProfile player) {
        super(manager, rank, player);
    }
}
