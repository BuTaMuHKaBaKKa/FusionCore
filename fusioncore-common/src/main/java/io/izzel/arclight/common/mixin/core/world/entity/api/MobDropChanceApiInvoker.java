package io.izzel.arclight.common.mixin.core.world.entity.api;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: the mob's drop chance per slot (body armor included) for Paper's {@code EntityEquipment#getDropChance} (patch 0512). */
@Mixin(Mob.class)
public interface MobDropChanceApiInvoker {

    @Invoker("getEquipmentDropChance")
    float fusioncore$getEquipmentDropChance(EquipmentSlot slot);
}
