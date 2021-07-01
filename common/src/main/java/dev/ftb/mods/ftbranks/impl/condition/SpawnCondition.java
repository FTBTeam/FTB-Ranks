package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftbranks.api.SimpleRankCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * @author LatvianModder
 */
public class SpawnCondition implements SimpleRankCondition {
	@Override
	public String getType() {
		return "spawn";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		if (player.level instanceof ServerLevel && player.level.dimension() == Level.OVERWORLD && player.server.getSpawnProtectionRadius() > 0) {
			BlockPos spawn = ((ServerLevel) player.level).getSharedSpawnPos();
			int x = Mth.abs(Mth.floor(player.getX()) - spawn.getX());
			int z = Mth.abs(Mth.floor(player.getZ()) - spawn.getZ());
			return Math.max(x, z) <= player.server.getSpawnProtectionRadius();
		}

		return false;
	}
}