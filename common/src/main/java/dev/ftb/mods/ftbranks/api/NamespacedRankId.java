package dev.ftb.mods.ftbranks.api;

import java.util.Optional;

/// Represents the combination of a rank ID, and the source it came from, i.e. whether this is server-local rank,
/// or defined by the modpack. This is important to avoid name clashes between server and modpack sourced ranks.
public record NamespacedRankId(RankFileSource source, String id) {
    public static NamespacedRankId serverRank(String id) {
        return new NamespacedRankId(RankFileSource.SERVER, id);
    }

    @Override
    public String toString() {
        return source.getId() + "." + id;
    }

    /// Create a namespaced rank ID from string. This method works as the inverse of [#toString()].
    ///
    ///  If no namespace prefix is provided, the SERVER namespace is assumed. If an invalid namespace is provided (a
    /// prefix other than "server." or "modpack.") then `Optional.empty()` is returned.
    ///
    /// @param idStr the rank ID string
    /// @return the namespace rank ID
    public static Optional<NamespacedRankId> fromString(String idStr) {
        String[] fields = idStr.split("\\.", 2);
        if (fields.length == 1) {
            return Optional.of(new NamespacedRankId(RankFileSource.SERVER, fields[0]));
        } else {
            RankFileSource source = RankFileSource.NAME_MAP.getNullable(fields[0]);
            return source == null ? Optional.empty() : Optional.of(new NamespacedRankId(source, fields[1]));
        }
    }
}
