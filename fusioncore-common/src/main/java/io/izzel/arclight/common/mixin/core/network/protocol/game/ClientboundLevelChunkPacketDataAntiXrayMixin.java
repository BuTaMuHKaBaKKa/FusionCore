package io.izzel.arclight.common.mixin.core.network.protocol.game;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreAntiXray;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Paper's Anti-Xray, the packet buffer and the order of the sections written into it
@Mixin(ClientboundLevelChunkPacketData.class)
public abstract class ClientboundLevelChunkPacketDataAntiXrayMixin {

    @Shadow @Final private byte[] buffer;
    @Unique private static final ThreadLocal<int[]> fusioncore$sectionCounter = ThreadLocal.withInitial(() -> new int[1]);

    @Inject(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;)V", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;extractChunkData(Lnet/minecraft/network/FriendlyByteBuf;Lnet/minecraft/world/level/chunk/LevelChunk;)V"))
    private void fusioncore$buffer(LevelChunk chunk, CallbackInfo ci) {
        var info = FusionCoreAntiXray.current();
        if (info != null) {
            info.setBuffer(this.buffer);
        }
        fusioncore$sectionCounter.get()[0] = 0;
    }

    @WrapOperation(method = "extractChunkData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;write(Lnet/minecraft/network/FriendlyByteBuf;)V"))
    private static void fusioncore$section(LevelChunkSection section, FriendlyByteBuf buf, Operation<Void> original) {
        if (FusionCoreAntiXray.current() == null) {
            original.call(section, buf);
            return;
        }
        int[] counter = fusioncore$sectionCounter.get();
        FusionCoreAntiXray.section(counter[0]++);
        try {
            original.call(section, buf);
        } finally {
            FusionCoreAntiXray.section(-1);
        }
    }
}
