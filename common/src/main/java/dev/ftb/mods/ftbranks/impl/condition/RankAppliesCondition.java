package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbranks.api.Rank;
import net.minecraft.server.level.ServerPlayer;

public class RankAppliesCondition extends RankAddedCondition {
    public RankAppliesCondition(Rank r, Json5Object json) {
        super(r, json);
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
