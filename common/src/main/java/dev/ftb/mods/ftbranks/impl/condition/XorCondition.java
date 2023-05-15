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

/**
 * @author LatvianModder
 */
public class XorCondition implements RankCondition {
	private final List<RankCondition> conditions;

	public XorCondition(Rank rank, SNBTCompoundTag tag) throws RankException {
		conditions = new ArrayList<>();

		for (Tag t : tag.getList("conditions", Tag.class)) {
			conditions.add(rank.getManager().createCondition(rank, t));
		}

		if (conditions.size() != 2) {
			throw new RuntimeException("XOR condition takes exactly two sub-conditions");
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