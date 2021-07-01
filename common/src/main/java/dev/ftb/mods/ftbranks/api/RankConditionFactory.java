package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

/**
 * @author LatvianModder
 */
public interface RankConditionFactory {
	RankCondition create(Rank rank, SNBTCompoundTag tag) throws Exception;
}