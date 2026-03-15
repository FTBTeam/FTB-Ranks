package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;
import net.minecraft.server.players.NameAndId;

/**
 * Fired when a player is removed from a rank, with the {@code /ftbranks remove} command.
 */
@FunctionalInterface
public interface PlayerRemovedFromRankEvent {
    void onPlayerRemovedFromRank(Data data);

    record Data(RankManager manager, Rank rank, NameAndId player) {}
}
