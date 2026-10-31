package io.izzel.arclight.common.mixin.core.world.level.chunk.storage;

import io.izzel.arclight.common.mod.util.FusionCoreWorldSettings;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.entity.ChunkEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

// Fusion Core: Paper's chunks.entity-per-chunk-save-limit (Paper patch 1000): a chunk saves and loads at most that many
// entities of a type; the rest are dropped (piles of arrows, snowballs, experience orbs)
@Mixin(EntityStorage.class)
public abstract class EntityStorageMixin {

    @org.spongepowered.asm.mixin.Shadow @org.spongepowered.asm.mixin.Final private net.minecraft.server.level.ServerLevel level;

    @ModifyVariable(method = "storeEntities", at = @At("HEAD"), argsOnly = true)
    private ChunkEntities<Entity> fusioncore$saveLimit(ChunkEntities<Entity> entities) {
        return FusionCoreWorldSettings.of(this.level).limitPerChunk(entities);
    }

    @Inject(method = "loadEntities", cancellable = true, at = @At("RETURN"))
    private void fusioncore$loadLimit(ChunkPos pos, CallbackInfoReturnable<CompletableFuture<ChunkEntities<Entity>>> cir) {
        var settings = FusionCoreWorldSettings.of(this.level);
        if (settings.hasSaveLimits()) {
            cir.setReturnValue(cir.getReturnValue().thenApply(settings::limitPerChunk));
        }
    }
}
