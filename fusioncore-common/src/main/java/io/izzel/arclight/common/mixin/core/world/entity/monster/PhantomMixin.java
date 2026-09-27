package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.bridge.core.world.entity.monster.PhantomBridge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Phantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

/**
 * Fusion Core: the player a phantom was spawned for (Paper's {@code Phantom#getSpawningEntity()}),
 * set by {@code PhantomSpawnerMixin} and kept in NBT under Paper's key.
 */
@Mixin(Phantom.class)
public abstract class PhantomMixin implements PhantomBridge {

    @Unique private UUID fusioncore$spawningEntity;

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$readSpawningEntity(CompoundTag tag, CallbackInfo ci) {
        if (tag.hasUUID("Paper.SpawningEntity")) {
            this.fusioncore$spawningEntity = tag.getUUID("Paper.SpawningEntity");
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void fusioncore$saveSpawningEntity(CompoundTag tag, CallbackInfo ci) {
        if (this.fusioncore$spawningEntity != null) {
            tag.putUUID("Paper.SpawningEntity", this.fusioncore$spawningEntity);
        }
    }

    @Override
    public UUID bridge$getSpawningEntity() {
        return this.fusioncore$spawningEntity;
    }

    @Override
    public void bridge$setSpawningEntity(UUID entity) {
        this.fusioncore$spawningEntity = entity;
    }
}
