package dev.ftb.mods.ftbranks.impl;

import com.mojang.authlib.GameProfile;
import dev.ftb.mods.ftblibrary.snbt.SNBTCompoundTag;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import dev.ftb.mods.ftbranks.api.*;
import dev.ftb.mods.ftbranks.api.event.*;
import dev.ftb.mods.ftbranks.impl.condition.AlwaysActiveCondition;
import dev.ftb.mods.ftbranks.impl.condition.DefaultCondition;
import org.jetbrains.annotations.NotNull;

import java.util.*;

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
		rank.setCondition(new DefaultCondition(rank));
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
		String displayName = tag.getStringOr("name", rankId);
		RankImpl rank = create(manager, rankId, displayName, tag.getIntOr("power", 0), source); // TODO: A default of 0 might not be ideal

		if (tag.contains("condition")) {
			rank.setCondition(manager.createCondition(rank, tag.get("condition")));
		}

		for (String key : tag.keySet()) {
			if (!SPECIAL_FIELDS.contains(key)) {
				while (key.endsWith(".*")) {
					key = key.substring(0, key.length() - 2);
					manager.markRanksDirty();
				}

				if (!key.isEmpty()) {
					rank.permissions.put(key, RankManagerImpl.ofTag(tag, key));
				}
			}
		}

		return rank;
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

		RankManagerImpl.writePermissions(permissions, res);

		return res;
	}

	public RankFileSource getSource() {
		return source;
	}
}
