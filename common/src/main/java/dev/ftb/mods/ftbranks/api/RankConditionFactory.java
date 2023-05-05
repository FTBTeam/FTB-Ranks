package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;

/**
 * A condition factory, used to create new instances of a condition. Register instances of this factory by
 * subscribing to {@link dev.ftb.mods.ftbranks.api.event.RegisterConditionsEvent}.
 */
@FunctionalInterface
public interface RankConditionFactory {
	/**
	 * Create a condition instance
	 *
	 * @param rank the rank to which this condition will apply
	 * @param tag the SNBT compound tag which stores serialized data for the condition
	 * @return a new condition instance
	 * @throws RankException if the rank could not be created for any reason
	 */
	RankCondition create(Rank rank, SNBTCompoundTag tag) throws RankException;
}