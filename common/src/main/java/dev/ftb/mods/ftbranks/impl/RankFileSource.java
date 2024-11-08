package dev.ftb.mods.ftbranks.impl;

import dev.architectury.platform.Platform;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.function.Function;

public enum RankFileSource {
    SERVER(server -> server.getWorldPath(RankManagerImpl.FOLDER_NAME).resolve("ranks.snbt")),
    MODPACK(server -> Platform.getConfigFolder().resolve("ftbranks-pack.snbt"));

    private final Function<MinecraftServer, Path> pathFunction;

    RankFileSource(Function<MinecraftServer, Path> pathFunction) {
        this.pathFunction = pathFunction;
    }

    public Path getPath(MinecraftServer server) {
        return pathFunction.apply(server);
    }
}
