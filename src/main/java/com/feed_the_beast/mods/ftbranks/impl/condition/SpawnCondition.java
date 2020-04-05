package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.dimension.DimensionType;

/**
 * @author LatvianModder
 */
public class SpawnCondition implements RankCondition
{
	@Override
	public String getType()
	{
		return "spawn";
	}

	@Override
	public boolean isRankActive(ServerPlayerEntity player)
	{
		if (player.dimension == DimensionType.OVERWORLD && player.server.getSpawnProtectionSize() > 0)
		{
			BlockPos spawn = player.world.getSpawnPoint();
			int x = MathHelper.abs(MathHelper.floor(player.getPosX()) - spawn.getX());
			int z = MathHelper.abs(MathHelper.floor(player.getPosZ()) - spawn.getZ());
			return Math.max(x, z) <= player.server.getSpawnProtectionSize();
		}

		return false;
	}
}