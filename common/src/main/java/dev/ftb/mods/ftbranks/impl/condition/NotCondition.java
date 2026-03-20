package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

public class NotCondition implements RankCondition {
	private final RankCondition condition;

	public NotCondition(Rank rank, Json5Object tag) throws RankException {
		condition = getConditionList(tag, "condition", rank).getFirst();
	}

	@Override
	public String getType() {
		return "not";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return !condition.isRankActive(player);
	}

	@Override
	public Json5Object save(Json5Object json) {
		Json5Object sub = new Json5Object();
		sub.addProperty("type", condition.getType());
		json.add("condition", condition.save(sub));
		return json;
	}
}