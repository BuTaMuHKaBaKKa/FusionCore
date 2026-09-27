package io.izzel.arclight.common.mixin.core.world.entity;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: living entity internals Paper's API reaches: jump API (patch 0316), setKiller (0147), active item (0204), slot break (0768), death sound (0242). */
@Mixin(LivingEntity.class)
public interface LivingEntityPaperAccessor {

    @Accessor("jumping")
    boolean fusioncore$jumping();

    @Accessor("lastHurtByPlayerTime")
    void fusioncore$setLastHurtByPlayerTime(int ticks);

    @Invoker("completeUsingItem")
    void fusioncore$completeUsingItem();

    @Invoker("getDeathSound")
    net.minecraft.sounds.SoundEvent fusioncore$getDeathSound();

    @Invoker("getSoundVolume")
    float fusioncore$getSoundVolume();

    @Invoker("entityEventForEquipmentBreak")
    static byte fusioncore$entityEventForEquipmentBreak(EquipmentSlot slot) {
        throw new AssertionError();
    }
}
