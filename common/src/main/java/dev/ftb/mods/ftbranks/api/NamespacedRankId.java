package dev.ftb.mods.ftbranks.api;

import java.util.Optional;

public record NamespacedRankId(RankFileSource source, String id) {
    public static NamespacedRankId serverRank(String id) {
        return new NamespacedRankId(RankFileSource.SERVER, id);
    }

    @Override
    public String toString() {
        return RankFileSource.NAME_MAP.getName(source) + "." + id;
    }

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
