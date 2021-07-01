package dev.ftb.mods.ftbranks.impl.condition;

import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.api.Rank;
import dev.ftb.mods.ftbranks.api.RankCondition;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author LatvianModder
 */
public class XorCondition implements RankCondition {
	public final List<RankCondition> conditions;

	public XorCondition(Rank rank, SNBTCompoundTag tag) throws Exception {
		conditions = new ArrayList<>();

		for (SNBTCompoundTag t : tag.getList("conditions", SNBTCompoundTag.class)) {
			conditions.add(rank.getManager().createCondition(rank, t));
		}
	}

	@Override
	public String getType() {
		return "xor";
	}

	@Override
	public boolean isRankActive(ServerPlayer player) {
		return conditions.get(0).isRankActive(player) != conditions.get(1).isRankActive(player);
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