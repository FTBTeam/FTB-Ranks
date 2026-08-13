package dev.ftb.mods.ftbranks.api;

import dev.ftb.mods.ftblibrary.platform.Platform;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbranks.impl.RankManagerImpl;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.function.Function;

/// Indicates where a rank has been loaded from.
///
/// * Ranks with a `SERVER` source are loaded from the world's
/// `serverconfig/ftbranks/ranks.json5` file and can be freely edited by the server admin, either via commands or
/// direct file editing.
/// * Ranks with a `MODPACK` are loaded from `config/ftbranks-pack.json5` and apply to all worlds in this game instance.
/// No attempt should be made to alter that file or any ranks loaded from that file. Such ranks are distributed as part
/// of the modpack, are required for smooth operation of the pack, and may be replaced whenever the pack is updated.
public enum RankFileSource {
    SERVER(server -> server.getWorldPath(RankManagerImpl.FOLDER_NAME).resolve("ranks.json5")),
    MODPACK(_ -> Platform.get().paths().configPath().resolve("ftbranks-pack.json5"));

    public static final NameMap<RankFileSource> NAME_MAP = NameMap.of(SERVER, RankFileSource.values()).create();

    private final Function<MinecraftServer, Path> pathFunction;

    RankFileSource(Function<MinecraftServer, Path> pathFunction) {
        this.pathFunction = pathFunction;
    }

    public Path getPath(MinecraftServer server) {
        return pathFunction.apply(server);
    }
}
