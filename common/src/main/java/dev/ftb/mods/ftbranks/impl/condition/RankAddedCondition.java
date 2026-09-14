package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.api.NamespacedRankId;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public class RankAddedCondition implements RankCondition {
	protected final Rank original;
	protected final NamespacedRankId id;

	public RankAddedCondition(Rank r, Json5Object json) {
		original = r;
		String idStr = Json5Util.getString(json, "rank").orElseThrow(() -> new RankException("missing 'id' field"));
		if (idStr.indexOf('.') < 0) {
			FTBRanks.LOGGER.warn("Assuming 'server.' namespace for referenced rank '{}' found in condition for rank '{}'. It is recommended to use an explicit namespace ('server.' or 'modpack.') prefix for referenced rank ID's.", idStr, original.getNamespacedId());

		}
		id = NamespacedRankId.fromString(idStr).orElseThrow();
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
	public Json5Object save(Json5Object json) {
		json.addProperty("rank", id.toString());
		return json;
	}

	@Override
	public List<NamespacedRankId> referencedRankIds() {
		return List.of(id);
	}
}
