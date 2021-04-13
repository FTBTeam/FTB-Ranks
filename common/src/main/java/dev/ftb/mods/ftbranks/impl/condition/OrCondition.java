package dev.ftb.mods.ftbranks.impl.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class OrCondition implements RankCondition {
	public final List<RankCondition> conditions;

	public OrCondition(Rank rank, JsonObject json) throws Exception {
		conditions = new ArrayList<>();

		for (JsonElement e : json.get("conditions").getAsJsonArray()) {
			conditions.add(rank.getManager().createCondition(rank, e.getAsJsonObject()));
		}
	}

	@Override
	public String getType() {
		return "or";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		for (RankCondition condition : conditions) {
			if (condition.isRankActive(player)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void save(JsonObject json) {
		JsonArray a = new JsonArray();

		for (RankCondition condition : conditions) {
			JsonObject c = new JsonObject();
			c.addProperty("type", condition.getType());
			condition.save(c);
			a.add(c);
		}

		json.add("conditions", a);
	}
}