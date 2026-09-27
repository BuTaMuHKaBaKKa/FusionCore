package io.izzel.arclight.common.mixin.core.world.entity;

import io.izzel.arclight.common.bridge.core.world.level.IWorldWriterBridge;
import io.izzel.arclight.common.mod.util.FusionCoreEnumConstants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.OminousItemSpawner;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: items and projectiles an ominous item spawner (ominous trial spawner loot) drops are spawned with Paper's
 * {@code SpawnReason.OMINOUS_ITEM_SPAWNER} (patch "Fixes and additions to the spawn reason API").
 */
@Mixin(OminousItemSpawner.class)
public abstract class OminousItemSpawnerMixin {

    @Inject(method = "spawnItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private void fusioncore$spawnReason(CallbackInfo ci) {
        ((IWorldWriterBridge) ((Entity) (Object) this).level()).bridge$pushAddEntityReason(FusionCoreEnumConstants.SpawnReason$OMINOUS_ITEM_SPAWNER());
    }
}
