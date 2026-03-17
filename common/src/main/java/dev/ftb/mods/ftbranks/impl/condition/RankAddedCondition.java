package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.util.Json5Util;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.UnknownNullability;

public class RankAddedCondition implements RankCondition {
	protected final Rank original;
	protected final String id;

	public RankAddedCondition(Rank r, Json5Object json) {
		original = r;
		id = Json5Util.getString(json, "rank").orElse("");
	}

	@Override
	public String getType() {
		return "rank_added";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return original.getManager().getRank(id)
				.map(rank -> rank != original && rank.isAdded(player))
				.orElse(false);
	}

	@Override
	public Json5Object save(@UnknownNullability Json5Object json) {
		json.addProperty("rank", id);
		return json;
	}
}
