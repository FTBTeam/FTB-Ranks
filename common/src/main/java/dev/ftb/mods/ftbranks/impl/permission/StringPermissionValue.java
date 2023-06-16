package dev.ftb.mods.ftbranks.impl.permission;

import dev.ftb.mods.ftbranks.api.PermissionValue;

import java.util.Optional;

/**
 * @author LatvianModder
 */
public class StringPermissionValue implements PermissionValue {
	public static final StringPermissionValue EMPTY = new StringPermissionValue("");

	public static StringPermissionValue of(String value) {
		return value.isEmpty() ? EMPTY : new StringPermissionValue(value);
	}

	public final String value;
	private final Optional<String> cachedValue;

	private StringPermissionValue(String v) {
		value = v;
		cachedValue = Optional.of(value);
	}

	@Override
	public Optional<String> asString() {
		return cachedValue;
	}

	@Override
	public String toString() {
		return value;
	}

	@Override
	public boolean equals(Object o) {
		return this == o || o instanceof StringPermissionValue s && value.equals(s.value);
	}

	@Override
	public int hashCode() {
		return value.hashCode();
	}
}