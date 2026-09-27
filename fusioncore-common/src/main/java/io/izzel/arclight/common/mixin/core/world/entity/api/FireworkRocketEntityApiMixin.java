package io.izzel.arclight.common.mixin.core.world.entity.api;

import io.izzel.arclight.common.bridge.core.world.entity.api.FireworkApiBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/** Fusion Core: Paper's {@code Firework#getSpawningEntity} (patch 0121): who launched the rocket, saved as {@code SpawningEntity}. */
@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityApiMixin implements FireworkApiBridge {

    @Unique private UUID fusioncore$spawningEntity;

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveSpawner(CompoundTag tag, CallbackInfo ci) {
        if (this.fusioncore$spawningEntity != null) {
            tag.putUUID("SpawningEntity", this.fusioncore$spawningEntity);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readSpawner(CompoundTag tag, CallbackInfo ci) {
        if (tag.hasUUID("SpawningEntity")) {
            this.fusioncore$spawningEntity = tag.getUUID("SpawningEntity");
        }
    }

    @Override
    public UUID fusioncore$spawningEntity() {
        return this.fusioncore$spawningEntity;
    }

    @Override
    public void fusioncore$setSpawningEntity(UUID spawningEntity) {
        this.fusioncore$spawningEntity = spawningEntity;
    }
}
