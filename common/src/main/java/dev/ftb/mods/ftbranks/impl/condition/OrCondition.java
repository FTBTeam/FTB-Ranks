package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class OrCondition implements RankCondition {
	private final List<RankCondition> conditions;

	public OrCondition(Rank rank, Json5Object json) throws RankException {
		conditions = getConditionList(json, "conditions", rank);
	}

	@Override
	public String getType() {
		return "or";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return conditions.stream().anyMatch(condition -> condition.isRankActive(player));
	}

	@Override
	public Json5Object save(Json5Object json) {
		Json5Array a = new Json5Array();

		for (RankCondition condition : conditions) {
			Json5Object c = new Json5Object();
			c.addProperty("type", condition.getType());
			a.add(condition.save(c));
		}

		json.add("conditions", a);
		return json;
	}
}