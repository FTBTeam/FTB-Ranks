package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;
import net.minecraft.server.players.NameAndId;

/**
 * Fired when a player is removed from a rank, with the {@code /ftbranks remove} command.
 */
public class PlayerRemovedFromRankEvent extends RankEvent.Player {
    public PlayerRemovedFromRankEvent(RankManagerImpl manager, Rank rank, NameAndId player) {
        super(manager, rank, player);
    }
}
