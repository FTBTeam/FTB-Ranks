package dev.ftb.mods.ftbranks.api.event;

import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankManager;
import net.minecraft.server.players.NameAndId;

import java.util.function.Consumer;

/// Fired when a player is added to a rank, with the `/ftbranks add` command.
@FunctionalInterface
public interface PlayerAddedToRankEvent extends Consumer<PlayerAddedToRankEvent.Data> {
    record Data(RankManager manager, Rank rank, NameAndId player) {}
}
