package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import net.minecraft.server.players.NameAndId;

/**
 * Fired when a player is added to a rank, with the {@code /ftbranks add} command.
 */
public class PlayerAddedToRankEvent extends RankEvent.Player {
    public PlayerAddedToRankEvent(RankManager manager, Rank rank, NameAndId player) {
        super(manager, rank, player);
    }
}
