package dev.ftb.mods.ftbranks.impl;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.platform.event.NativeEventPosting;
import dev.ftb.mods.ftblibrary.util.Json5Util;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.ConditionChangedEvent;
import dev.ftb.mods.ftbranks.api.event.PermissionNodeChangedEvent;
import dev.ftb.mods.ftbranks.api.event.PlayerAddedToRankEvent;
import dev.ftb.mods.ftbranks.api.event.PlayerRemovedFromRankEvent;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import net.minecraft.server.players.NameAndId;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class RankImpl implements Rank, Comparable<RankImpl> {
	private static final Set<String> SPECIAL_FIELDS = Set.of("name", "power", "condition");

	private final RankManagerImpl manager;
	private final String id;
	private final Map<String, PermissionValue> permissions = new LinkedHashMap<>();
	private final String name;
	private final int power;
	private final RankFileSource source;
	private RankCondition condition;

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, RankCondition condition, RankFileSource source) {
		return new RankImpl(manager, id, name, power, condition, source);
	}

	public static RankImpl create(RankManagerImpl manager, String id, String name, int power, RankFileSource source) {
		RankImpl rank = new RankImpl(manager, id, name, power, AlwaysActiveCondition.INSTANCE, source);
		rank.setCondition(new DefaultCondition(rank));
		return rank;
	}

	private RankImpl(RankManagerImpl manager, String id, String name, int power, RankCondition condition, RankFileSource source) {
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
	public void setPermission(String node, @Nullable PermissionValue value) {
		if (node.equals("condition")) {
			throw new IllegalArgumentException("use '/ftbranks condition' to set conditions");
		}

		PermissionValue oldValue = getPermission(node);
		if (!oldValue.equals(value)) {
			if (value != null) {
				permissions.put(node, value);
			} else {
				permissions.remove(node);
			}
			NativeEventPosting.INSTANCE.postEvent(new PermissionNodeChangedEvent.Data(manager, this, node, oldValue, value));
			if (node.equals("ftbranks.name_format")) {
				PlayerNameFormatting.refreshPlayerNames(manager.getServer());
			}
			manager.markRanksDirty();
		}
	}

	@Override
	public PermissionValue getPermission(String node) {
		return permissions.getOrDefault(node, PermissionValue.MISSING);
	}

	@Override
	public RankCondition getCondition() {
		return condition;
	}

	@Override
	public void setCondition(RankCondition newCondition) {
		RankCondition oldCondition = this.condition;
		this.condition = newCondition;
		NativeEventPosting.INSTANCE.postEvent(new ConditionChangedEvent.Data(manager, this, oldCondition, newCondition));
		PlayerNameFormatting.refreshPlayerNames(manager.getServer());
		manager.markRanksDirty();
	}

	@Override
	public boolean add(NameAndId nameAndId) {
		if (manager.getOrCreatePlayerData(nameAndId).addRank(this)) {
			NativeEventPosting.INSTANCE.postEvent(new PlayerAddedToRankEvent.Data(manager, this, nameAndId));
			PlayerNameFormatting.refreshPlayerNames(manager.getServer());
			return true;
		}

		return false;
	}

	@Override
	public boolean remove(NameAndId nameAndId) {
		if (manager.getOrCreatePlayerData(nameAndId).removeRank(this)) {
			manager.markPlayerDataDirty();
			NativeEventPosting.INSTANCE.postEvent(new PlayerRemovedFromRankEvent.Data(manager, this, nameAndId));
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
		RankImpl rank = create(manager, rankId, displayName, Json5Util.getInt(json,"power").orElse(0), source); // TODO: A default of 0 might not be ideal

		if (json.has("condition")) {
			rank.setCondition(manager.createCondition(rank, json.get("condition")));
		}

		for (String key : json.keySet()) {
			if (!SPECIAL_FIELDS.contains(key)) {
				while (key.endsWith(".*")) {
					key = key.substring(0, key.length() - 2);
					manager.markRanksDirty();
				}

				if (!key.isEmpty()) {
					rank.permissions.put(key, RankManagerImpl.readPermissions(json, key));
				}
			}
		}

		return rank;
	}

	public Json5Object toJson() {
		Json5Object res = new Json5Object();

		res.addProperty("name", name);
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

		RankManagerImpl.writePermissions(permissions, res);

		return res;
	}

	public RankFileSource getSource() {
		return source;
	}
}
