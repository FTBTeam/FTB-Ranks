package dev.ftb.mods.ftbranks.impl.permission;

import dev.ftb.mods.ftbranks.api.PermissionValue;

import java.util.Optional;

public class BooleanPermissionValue implements PermissionValue {
	public static final BooleanPermissionValue TRUE = new BooleanPermissionValue(true);
	public static final BooleanPermissionValue FALSE = new BooleanPermissionValue(false);

	public static BooleanPermissionValue of(boolean value) {
		return value ? TRUE : FALSE;
	}

	public final boolean value;
	private final Boolean cachedValue;

	private BooleanPermissionValue(boolean v) {
		value = v;
		cachedValue = value;
	}

	@Override
	public Optional<Boolean> asBoolean() {
		return Optional.of(cachedValue);
	}

	@Override
	public boolean asBooleanOrTrue() {
		return value;
	}

	@Override
	public boolean asBooleanOrFalse() {
		return value;
	}

	@Override
	public String toString() {
		return value ? "true" : "false";
	}

	@Override
	public boolean equals(Object o) {
		return this == o;
	}

	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}
}