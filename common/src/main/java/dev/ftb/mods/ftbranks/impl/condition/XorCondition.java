package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Array;
import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class XorCondition implements RankCondition {
	private final List<RankCondition> conditions;

	public XorCondition(Rank rank, Json5Object json) throws RankException {
		conditions = getConditionList(json, "conditions", rank);
		if (conditions.size() != 2) {
			throw new RuntimeException("XOR condition takes exactly two sub-conditions");
		}
	}

	@Override
	public String getType() {
		return "xor";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return conditions.get(0).isRankActive(player) != conditions.get(1).isRankActive(player);
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