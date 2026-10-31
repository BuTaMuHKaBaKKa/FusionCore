package io.izzel.arclight.common.mixin.core.network.protocol.game;

import com.destroystokyo.paper.antixray.ChunkPacketInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreAntiXray;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

// Fusion Core: Paper's Anti-Xray rewrites the block bits of the chunk data after it is built, on a background thread; the
// packet waits for it right before it is encoded, so packets keep their order (FusionCoreAntiXray)
@Mixin(ClientboundLevelChunkWithLightPacket.class)
public abstract class ClientboundLevelChunkWithLightPacketAntiXrayMixin implements FusionCoreAntiXray.PendingPacket {

    @Unique private volatile CompletableFuture<Void> fusioncore$antiXrayTask;

    @WrapOperation(method = "<init>(Lnet/minecraft/world/level/chunk/LevelChunk;Lnet/minecraft/world/level/lighting/LevelLightEngine;Ljava/util/BitSet;Ljava/util/BitSet;)V",
        at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/chunk/LevelChunk;)Lnet/minecraft/network/protocol/game/ClientboundLevelChunkPacketData;"))
    private ClientboundLevelChunkPacketData fusioncore$antiXray(LevelChunk chunk, Operation<ClientboundLevelChunkPacketData> original) {
        ClientboundLevelChunkWithLightPacket packet = (ClientboundLevelChunkWithLightPacket) (Object) this;
        ChunkPacketInfo<BlockState> info = FusionCoreAntiXray.begin(packet, chunk);
        ClientboundLevelChunkPacketData data;
        try {
            data = original.call(chunk);
        } catch (Throwable t) {
            FusionCoreAntiXray.end(packet, chunk, null);
            throw t;
        }
        FusionCoreAntiXray.end(packet, chunk, info);
        return data;
    }

    @Inject(method = "write", at = @At("HEAD"))
    private void fusioncore$awaitObfuscation(RegistryFriendlyByteBuf buf, CallbackInfo ci) {
        this.fusioncore$awaitAntiXray();
    }

    @Override
    public void fusioncore$setAntiXrayTask(CompletableFuture<Void> task) {
        this.fusioncore$antiXrayTask = task;
    }

    @Override
    public void fusioncore$awaitAntiXray() {
        CompletableFuture<Void> task = this.fusioncore$antiXrayTask;
        if (task != null) {
            task.join();
        }
    }
}
