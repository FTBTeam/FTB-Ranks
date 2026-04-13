package dev.ftb.mods.ftbranks.api;

import de.marhali.json5.Json5Object;

/// A condition factory, used to create new instances of a condition. Register instances of this factory by
/// subscribing to [dev.ftb.mods.ftbranks.api.event.RegisterConditionsEvent].
@FunctionalInterface
public interface RankConditionFactory {
	/// Create a condition instance
	///
	/// @param rank the rank to which this condition will apply
	/// @param json the Json5 object which stores serialized data for the condition
	/// @return a new condition instance
	/// @throws RankException if the rank could not be created for any reason
	RankCondition create(Rank rank, Json5Object json) throws RankException;
}