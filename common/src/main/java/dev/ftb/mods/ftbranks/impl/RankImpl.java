package dev.ftb.mods.ftbranks.impl;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import dev.ftb.mods.ftbranks.impl.permission.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.nbt.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static dev.ftb.mods.ftbranks.FTBRanks.LOGGER;

public class RankImpl implements Rank, Comparable<RankImpl> {
	private static final Set<String> SPECIAL_FIELDS = Set.of("name", "power", "condition");

	private final RankManagerImpl manager;
	private final String id;
	private final Map<String, PermissionValue> permissions = new LinkedHashMap<>();
	private final String name;
	private final int power;
	private final RankFileSource source;
	@NotNull
	private RankCondition condition;

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, @NotNull RankCondition condition, RankFileSource source) {
		return new RankImpl(manager, id, name, power, condition, source);
	}

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, RankFileSource source) {
		RankImpl rank = new RankImpl(manager, id, name, power, AlwaysActiveCondition.INSTANCE, source);
		rank.condition = new DefaultCondition(rank);
		return rank;
	}

	private RankImpl(RankManagerImpl manager, String id, String name, int power, @NotNull RankCondition condition, RankFileSource source) {
		this.manager = manager;
		this.id = id;
		this.name = name;
		this.power = power;
		this.condition = condition;
		this.source = source;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || o instanceof Rank && id.equals(((Rank) o).getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public RankManager getManager() {
		return manager;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getPower() {
		return power;
	}

	@Override
	public void setPermission(String node, PermissionValue value) {
		if (SPECIAL_FIELDS.contains(node)) {
			String err = "'" + node + "' is a reserved field";
			if (node.equals("condition")) {
				err += " (use '/ftbranks condition' to set conditions)";
			}
			throw new IllegalArgumentException(err);
		}

		PermissionValue oldValue = getPermission(node);
		if (!oldValue.equals(value)) {
			if (value != null) {
				permissions.put(node, value);
			} else {
				permissions.remove(node);
			}
			RankEvent.PERMISSION_CHANGED.invoker().accept(new PermissionNodeChangedEvent(manager, this, node, oldValue, value));
			if (node.equals("ftbranks.name_format")) {
				PlayerNameFormatting.refreshPlayerNames();
			}
			manager.markRanksDirty();
		}
	}

	@Override
	@NotNull
	public PermissionValue getPermission(String node) {
		return permissions.getOrDefault(node, PermissionValue.MISSING);
	}

	@Override
	@NotNull
	public RankCondition getCondition() {
		return condition;
	}

	@Override
	public void setCondition(RankCondition newCondition) {
		RankCondition oldCondition = this.condition;
		this.condition = newCondition;
		RankEvent.CONDITION_CHANGED.invoker().accept(new ConditionChangedEvent(manager, this, oldCondition, newCondition));
		PlayerNameFormatting.refreshPlayerNames();
		manager.markRanksDirty();
	}

	@Override
	public boolean add(GameProfile profile) {
		if (manager.getOrCreatePlayerData(profile).addRank(this)) {
			RankEvent.ADD_PLAYER.invoker().accept(new PlayerAddedToRankEvent(manager, this, profile));
			PlayerNameFormatting.refreshPlayerNames();
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(GameProfile profile) {
		if (manager.getOrCreatePlayerData(profile).removeRank(this)) {
			manager.markPlayerDataDirty();
			RankEvent.REMOVE_PLAYER.invoker().accept(new PlayerRemovedFromRankEvent(manager,this, profile));
			PlayerNameFormatting.refreshPlayerNames();
			return true;
		}

		return false;
	}

	@Override
	public int compareTo(RankImpl o) {
		return o.getPower() - getPower();
	}

	@Override
	public Collection<String> getPermissions() {
		Set<String> nodes = new HashSet<>(permissions.keySet());
		nodes.removeAll(SPECIAL_FIELDS);
		return nodes;
	}

	public static RankImpl readSNBT(RankManagerImpl manager, String rankId, SNBTCompoundTag tag, RankFileSource source) throws RankException {
		String displayName = tag.getString("name").isEmpty() ? rankId : tag.getString("name");
		RankImpl rank = create(manager, rankId, displayName, tag.getInt("power"), source);

		if (tag.contains("condition")) {
			rank.condition = manager.createCondition(rank, tag.get("condition"));
		}

		for (String key : tag.getAllKeys()) {
            if (!key.isEmpty() && !SPECIAL_FIELDS.contains(key)) {
				readPermissions(tag, key).ifPresentOrElse(
						perm -> rank.permissions.put(stripLegacyPermNodeSuffix(key), perm),
						() -> FTBRanks.LOGGER.warn("readPermissions: ignoring non-primitive member {} of rank {}", key, rankId)
				);
            }
		}

		return rank;
	}

	private static String stripLegacyPermNodeSuffix(String key) {
		// legacy ".*" suffix on command permission nodes is no longer required
		while (key.endsWith(".*")) {
			key = key.substring(0, key.length() - 2);
		}
		return key;
	}

	public SNBTCompoundTag writeSNBT() {
		SNBTCompoundTag res = new SNBTCompoundTag();

		res.putString("name", name);
		res.putInt("power", power);

		if (!condition.isDefaultCondition()) {
			if (condition.isSimple()) {
				res.putString("condition", condition.getType());
			} else {
				SNBTCompoundTag c = new SNBTCompoundTag();
				c.putString("type", condition.getType());
				condition.save(c);
				res.put("condition", c);
			}
		}

		writePermissions(permissions, res);

		return res;
	}

	public RankFileSource getSource() {
		return source;
	}

	private static Optional<PermissionValue> readPermissions(SNBTCompoundTag tag, String key) {
		Tag v = tag.get(key);

		if (v == null || v instanceof ListTag || v instanceof CompoundTag) {
			return Optional.empty();
		}

		if (tag.isBoolean(key)) {
			return Optional.of(BooleanPermissionValue.of(tag.getBoolean(key)));
		}

		return switch (v) {
			case NumericTag numericTag -> Optional.of(NumberPermissionValue.of(numericTag.getAsNumber()));
			case StringTag stringTag -> Optional.of(StringPermissionValue.of(stringTag.getAsString()));
			default -> Optional.empty();
		};
	}

	private static void writePermissions(Map<String, PermissionValue> map, SNBTCompoundTag res) {
		map.forEach((key, value) -> {
            switch (value) {
                case BooleanPermissionValue b -> res.putBoolean(key, b.value);
                case StringPermissionValue s -> res.putString(key, s.value);
                case NumberPermissionValue n -> res.putNumber(key, n.value);
                default -> LOGGER.warn("writePermissions: ignoring unknown perm val {} (class {})", key, value.getClass().getName());
            }
		});
	}

}