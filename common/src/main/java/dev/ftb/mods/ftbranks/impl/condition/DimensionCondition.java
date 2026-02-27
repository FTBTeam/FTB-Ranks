package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class DimensionCondition implements RankCondition {
	private final ResourceKey<Level> dimension;

	public DimensionCondition(SNBTCompoundTag tag) {
		dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(tag.getStringOr("dimension", "")));
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
	public void save(SNBTCompoundTag tag) {
		tag.putString("dimension", dimension.identifier().toString());
	}
}
