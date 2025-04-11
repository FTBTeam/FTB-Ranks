package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.server.level.ServerPlayer;

public class RankAppliesCondition extends RankAddedCondition {
    public RankAppliesCondition(Rank r, SNBTCompoundTag tag) {
        super(r, tag);
    }

    @Override
    public String getType() {
        return "rank_applies";
    }

    @Override
    public boolean isRankActive(ServerPlayer player) {
        return original.getManager().getRank(id)
                .map(rank -> rank != original && rank.isActive(player))
                .orElse(false);
    }
}
