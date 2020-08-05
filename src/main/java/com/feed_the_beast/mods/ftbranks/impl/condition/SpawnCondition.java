package com.feed_the_beast.mods.ftbranks.impl.condition;

import com.feed_the_beast.mods.ftbranks.api.RankCondition;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
		if (player.world instanceof ServerWorld && player.world.func_234923_W_() == World.field_234918_g_ && player.server.getSpawnProtectionSize() > 0)
		{
			BlockPos spawn = ((ServerWorld) player.world).func_241135_u_();
			int x = MathHelper.abs(MathHelper.floor(player.getPosX()) - spawn.getX());
			int z = MathHelper.abs(MathHelper.floor(player.getPosZ()) - spawn.getZ());
			return Math.max(x, z) <= player.server.getSpawnProtectionSize();
		}

		return false;
	}
}