package dev.ftb.mods.ftbranks.core.mixin;

import dev.ftb.mods.ftbranks.impl.FTBRanksCommandManager;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
	@Inject(method = "<init>", at = @At("RETURN"))
	private void initFTBR(Commands.CommandSelection commandSelection, CallbackInfo ci) {
		FTBRanksCommandManager.INSTANCE = new FTBRanksCommandManager((Commands) (Object) this);
	}
}
