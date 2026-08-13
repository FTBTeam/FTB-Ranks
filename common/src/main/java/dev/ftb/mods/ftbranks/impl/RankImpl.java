package dev.ftb.mods.ftbranks.impl;

import de.marhali.json5.Json5Element;
import de.marhali.json5.Json5Object;
import de.marhali.json5.Json5Primitive;
import dev.ftb.mods.ftblibrary.json5.Json5Util;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.ConditionChangedEvent;
import dev.ftb.mods.ftbranks.api.event.PermissionNodeChangedEvent;
import dev.ftb.mods.ftbranks.api.event.PlayerAddedToRankEvent;
import dev.ftb.mods.ftbranks.api.event.PlayerRemovedFromRankEvent;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import dev.ftb.mods.ftbranks.impl.permission.BooleanPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.NumberPermissionValue;
import dev.ftb.mods.ftbranks.impl.permission.StringPermissionValue;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static dev.ftb.mods.ftbranks.FTBRanks.LOGGER;

public class RankImpl implements Rank, Comparable<RankImpl> {
	private static final Set<String> SPECIAL_FIELDS = Set.of("name", "power", "condition");

	private final RankManagerImpl manager;
	private final String id;
	private final Map<String, PermissionValue> permissions = new LinkedHashMap<>();
	private final RankFileSource source;
	private final NamespacedRankId namespacedRankId;
	private RankCondition condition;
	private String displayName;
	private int power;

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, RankCondition condition, RankFileSource source) {
		return new RankImpl(manager, id, name, power, condition, source);
	}

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, RankFileSource source) {
		RankImpl rank = new RankImpl(manager, id, name, power, AlwaysActiveCondition.INSTANCE, source);
		rank.condition = new DefaultCondition(rank);  // don't use setCondition() here
		return rank;
	}

	private RankImpl(RankManagerImpl manager, String id, String displayName, int power, RankCondition condition, RankFileSource source) {
		this.manager = manager;
		this.id = id;
		this.displayName = displayName;
		this.power = power;
		this.condition = condition;
		this.source = source;
		this.namespacedRankId = new NamespacedRankId(source, id);
	}

	@Override
	public NamespacedRankId getNamespacedId() {
		return namespacedRankId;
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
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(String displayName) {
		requireSource(RankFileSource.SERVER, "cannot change display name");

		this.displayName = displayName;

		manager.markRanksDirty();
	}

	@Override
	public int getPower() {
		return power;
	}

	@Override
	public void setPower(int power) {
		requireSource(RankFileSource.SERVER, "cannot change power");

		this.power = power;

		manager.rebuildSortedRanks();
		manager.markRanksDirty();
	}

	@Override
	public void setPermission(String node, @Nullable PermissionValue value) {
		requireSource(RankFileSource.SERVER, "cannot change permission nodes");

		if (SPECIAL_FIELDS.contains(node)) {
			String err = "'" + node + "' is a reserved field";
			if (node.equals("condition")) {
				err += " (use '/ftbranks condition' to set conditions)";
			}
			throw new IllegalArgumentException(err);
		}

		PermissionValue oldValue = getPermission(node);
		if (!areValuesEquivalent(value, oldValue)) {
			if (value != null) {
				permissions.put(node, value);
			} else {
				permissions.remove(node);
			}
			NativeEventPosting.get().postEvent(new PermissionNodeChangedEvent.Data(manager, this, node, oldValue, value));
			if (node.equals("ftbranks.name_format")) {
				PlayerNameFormatting.refreshPlayerNames(manager.getServer());
			}
			manager.markRanksDirty();
		}
	}

	private boolean areValuesEquivalent(@Nullable PermissionValue value1, PermissionValue value2) {
		return value1 == null && value2 == PermissionValue.MISSING || value2.equals(value1);
	}

	@Override
	public PermissionValue getPermission(String node) {
		return permissions.getOrDefault(node, PermissionValue.MISSING);
	}

	@Override
	public RankCondition getCondition() {
		return condition;
	}

	private void requireSource(RankFileSource source, String err) {
		if (getSource() != source) {
			throw new RankException(err + " (source: " + getSource() + ")");
		}
	}

	@Override
	public void setCondition(RankCondition newCondition) {
		requireSource(RankFileSource.SERVER, "cannot change condition");

		RankCondition oldCondition = this.condition;
		this.condition = newCondition;
		NativeEventPosting.get().postEvent(new ConditionChangedEvent.Data(manager, this, oldCondition, newCondition));
		PlayerNameFormatting.refreshPlayerNames(manager.getServer());
		manager.markRanksDirty();
	}

	@Override
	public boolean add(NameAndId nameAndId) {
		if (!getCondition().isDefaultCondition()) {
			throw new RankException("rank must not have a condition set");
		}

		if (manager.getOrCreatePlayerData(nameAndId).addRank(this)) {
			NativeEventPosting.get().postEvent(new PlayerAddedToRankEvent.Data(manager, this, nameAndId));
			PlayerNameFormatting.refreshPlayerNames(manager.getServer());
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(NameAndId nameAndId) {
		if (manager.getOrCreatePlayerData(nameAndId).removeRank(this)) {
			manager.markPlayerDataDirty();
			NativeEventPosting.get().postEvent(new PlayerRemovedFromRankEvent.Data(manager, this, nameAndId));
			PlayerNameFormatting.refreshPlayerNames(manager.getServer());
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

	public static RankImpl fromJson(RankManagerImpl manager, String rankId, Json5Object json, RankFileSource source) throws RankException {
		String displayName = Json5Util.getString(json, "name").orElse(rankId);
		int power = Json5Util.getInt(json, "power").orElse(1);
		if (power <= 0) {
			LOGGER.warn("invalid power level {} for rank {} (must be >= 1) - setting to 1", power, rankId);
			power = 1;
			manager.markRanksDirty();
		}

		RankImpl rank = create(manager, rankId, displayName, power, source);
		if (json.has("condition")) {
			rank.condition = manager.createCondition(rank, json.get("condition"));
		}

		for (String key : json.keySet()) {
            if (!key.isEmpty() && !SPECIAL_FIELDS.contains(key)) {
                readPermissions(json, key).ifPresentOrElse(
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

	public Json5Object toJson() {
		Json5Object res = new Json5Object();

		res.addProperty("name", displayName);
		res.addProperty("power", power);

		if (!condition.isDefaultCondition()) {
			if (condition.isSimple()) {
				res.addProperty("condition", condition.getType());
			} else {
				res.add("condition", Util.make(new Json5Object(), json -> {
					json.addProperty("type", condition.getType());
					condition.save(json);
				}));
			}
		}

		writePermissions(permissions, res);

		return res;
	}

	@Override
	public RankFileSource getSource() {
		return source;
	}

	private static Optional<PermissionValue> readPermissions(Json5Object json, String key) {
		Json5Element el = json.get(key);

		if (el == null || !el.isJson5Primitive()) {
			return Optional.empty();
		}

		Json5Primitive primitive = el.getAsJson5Primitive();
		if (primitive.isBoolean()) {
			return Optional.of(BooleanPermissionValue.of(primitive.getAsBoolean()));
		} else if (primitive.isNumber()) {
			return Optional.of(NumberPermissionValue.of(primitive.getAsNumber()));
		} else {
			return Optional.of(StringPermissionValue.of(primitive.getAsString()));
		}
	}

	private static void writePermissions(Map<String, PermissionValue> map, Json5Object res) {
		map.forEach((key, value) -> {
			switch (value) {
				case BooleanPermissionValue b -> res.addProperty(key, b.value);
				case StringPermissionValue s -> res.addProperty(key, s.value);
				case NumberPermissionValue n -> res.addProperty(key, n.value);
				default -> LOGGER.warn("writePermissions: ignoring unknown perm val {} (class {})", key, value.getClass().getName());
			}
		});
	}

}
