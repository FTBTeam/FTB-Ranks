package dev.ftb.mods.ftbranks.core.mixin.fabric;

import dev.ftb.mods.ftbranks.PlayerDisplayNameCache;
import dev.ftb.mods.ftbranks.PlayerNameFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(Player.class)
public abstract class PlayerMixin implements PlayerDisplayNameCache {
    private Component cachedDisplayName = null;

    @Inject(method = "getDisplayName", at = @At("RETURN"), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Component> cir, MutableComponent mutableComponent) {
        if (cachedDisplayName == null) cachedDisplayName = PlayerNameFormatting.formatPlayerName((Player) (Object)this, mutableComponent);
        cir.setReturnValue(cachedDisplayName);
    }

    @Override
    public void clearCachedDisplayName() {
        cachedDisplayName = null;
    }
}
