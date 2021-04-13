package dev.ftb.mods.ftbranks.impl.condition;

import com.google.gson.JsonObject;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public final class RankAddedCondition implements RankCondition {
	public final Rank original;
	public final String id;

	public RankAddedCondition(Rank r, JsonObject json) {
		original = r;
		id = json.get("rank").getAsString();
	}

	@Override
	public String getType() {
		return "rank_added";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		Optional<Rank> rank = original.getManager().getRank(id);
		Rank r = rank.orElse(null);
		return r != null && r != original && r.isAdded(player);
	}
}
