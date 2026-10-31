package io.izzel.arclight.common.mixin.core.world.level.chunk;

import io.izzel.arclight.common.mod.util.FusionCoreAntiXray;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.chunk.Palette;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Paper's Anti-Xray, where the block bits of a section start in the chunk packet (Paper patch 0992)
@Mixin(targets = "net.minecraft.world.level.chunk.PalettedContainer$Data")
public abstract class PalettedContainerDataAntiXrayMixin {

    @Shadow @Final private BitStorage storage;
    @Shadow @Final private Palette<?> palette;

    @Inject(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/FriendlyByteBuf;writeLongArray([J)Lnet/minecraft/network/FriendlyByteBuf;"))
    private void fusioncore$record(FriendlyByteBuf buf, CallbackInfo ci) {
        if (FusionCoreAntiXray.current() != null) {
            FusionCoreAntiXray.recordData(buf, this.storage, this.palette);
        }
    }
}
