package dev.ftb.mods.ftbranks.api;

/**
 * @author LatvianModder
 */
public interface SimpleRankCondition extends RankCondition {
	@Override
	default boolean isSimple() {
		return true;
	}
}