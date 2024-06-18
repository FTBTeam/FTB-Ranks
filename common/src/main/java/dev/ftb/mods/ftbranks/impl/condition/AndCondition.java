package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import dev.ftb.mods.ftbranks.api.RankException;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class AndCondition implements RankCondition {
	private final List<RankCondition> conditions;

	public AndCondition(Rank rank, SNBTCompoundTag tag) throws RankException {
		conditions = new ArrayList<>();

		for (Tag t : tag.getList("conditions", Tag.class)) {
			conditions.add(rank.getManager().createCondition(rank, t));
		}
	}

	@Override
	public String getType() {
		return "and";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return conditions.stream().allMatch(condition -> condition.isRankActive(player));
	}

	@Override
	public void save(SNBTCompoundTag tag) {
		ListTag a = new ListTag();

		for (RankCondition condition : conditions) {
			SNBTCompoundTag c = new SNBTCompoundTag();
			c.putString("type", condition.getType());
			condition.save(c);
			a.add(c);
		}

		tag.put("conditions", a);
	}
}