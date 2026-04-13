package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;
import net.minecraft.server.players.NameAndId;

import java.util.function.Consumer;

/// Fired when a player is removed from a rank, with the `/ftbranks remove` command.
@FunctionalInterface
public interface PlayerRemovedFromRankEvent extends Consumer<PlayerRemovedFromRankEvent.Data> {
    record Data(RankManager manager, Rank rank, NameAndId player) {}
}
