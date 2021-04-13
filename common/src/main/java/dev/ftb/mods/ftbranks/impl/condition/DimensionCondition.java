package dev.ftb.mods.ftbranks.impl.condition;

import com.google.gson.JsonObject;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * @author LatvianModder
 */
public class DimensionCondition implements RankCondition {
	public final ResourceKey<Level> dimension;

	public DimensionCondition(JsonObject json) {
		dimension = ResourceKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(json.get("dimension").getAsString()));
	}

	@Override
	public String getType() {
		return "dimension";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return player.level.dimension() == dimension;
	}

	@Override
	public void save(JsonObject json) {
		json.addProperty("dimension", dimension.location().toString());
	}
}