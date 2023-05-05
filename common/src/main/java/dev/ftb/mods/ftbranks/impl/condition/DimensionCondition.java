package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * @author LatvianModder
 */
public class DimensionCondition implements RankCondition {
	public final ResourceKey<Level> dimension;

	public DimensionCondition(SNBTCompoundTag tag) {
		dimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("dimension")));
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
	public void save(SNBTCompoundTag tag) {
		tag.putString("dimension", dimension.location().toString());
	}
}