package dev.ftb.mods.ftbranks.impl.condition;

import com.google.gson.JsonObject;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.server.level.ServerPlayer;

/**
 * @author LatvianModder
 */
public class NotCondition implements RankCondition {
	public final RankCondition condition;

	public NotCondition(Rank rank, JsonObject json) throws Exception {
		condition = rank.getManager().createCondition(rank, json.get("condition").getAsJsonObject());
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
	public void save(JsonObject json) {
		JsonObject c = new JsonObject();
		c.addProperty("type", condition.getType());
		condition.save(c);
		json.add("condition", c);
	}
}