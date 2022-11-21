package dev.ftb.mods.ftbranks.forge;

import dev.ftb.mods.ftbranks.FTBRanks;
import dev.ftb.mods.ftbranks.RankArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.Registry;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModArgumentTypeInfo {
    public static final DeferredRegister<ArgumentTypeInfo<?,?>> ARGUMENT_TYPE_INFO
            = DeferredRegister.create(Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, FTBRanks.MOD_ID);

    public static final RegistryObject<RankArgumentType.Info> RANK_COMMAND_ARGUMENT_TYPE
            = ARGUMENT_TYPE_INFO.register("rank", () -> ArgumentTypeInfos.registerByClass(RankArgumentType.class, new RankArgumentType.Info()));
}
