package dev.ftb.mods.ftbranks.impl.condition;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class DimensionCondition implements RankCondition {
	private final ResourceKey<Level> dimension;

	public DimensionCondition(Json5Object json) {
		dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(Json5Util.getString(json,"dimension").orElse("minecraft:overworld")));
	}

	@Override
	public String getType() {
		return "dimension";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.level().dimension() == dimension;
	}

	@Override
	public Json5Object save(Json5Object json) {
		json.addProperty("dimension", dimension.identifier().toString());
		return json;
	}
}
