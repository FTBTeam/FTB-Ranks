package dev.ftb.mods.ftbranks.api;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * @author LatvianModder
 */
public interface PermissionValue {
	PermissionValue DEFAULT = new PermissionValue() {
		@Override
		public boolean isDefaultValue() {
			return true;
		}

		@Override
		public String toString() {
			return "default";
		}
	};

	default boolean isDefaultValue() {
		return false;
	}

	default Optional<String> asString() {
		return Optional.empty();
	}

	default OptionalBoolean asBoolean() {
		return OptionalBoolean.EMPTY;
	}

	default boolean asBooleanOrTrue() {
		return asBoolean().orElse(true);
	}

	default boolean asBooleanOrFalse() {
		return asBoolean().orElse(false);
	}

	default Optional<Number> asNumber() {
		return Optional.empty();
	}

	default OptionalInt asInteger() {
		return OptionalInt.empty();
	}

	default OptionalLong asLong() {
		return OptionalLong.empty();
	}

	default OptionalDouble asDouble() {
		return OptionalDouble.empty();
	}
}