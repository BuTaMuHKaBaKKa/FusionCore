package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Creeper#isIgnited()} and {@code Creeper#setIgnited(boolean)} (fires CreeperIgniteEvent). */
@Mixin(value = org.bukkit.entity.Creeper.class, remap = false)
public interface CreeperMixin {

    default boolean isIgnited() {
        return ((net.minecraft.world.entity.monster.Creeper) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).isIgnited();
    }

    default void setIgnited(boolean ignited) {
        ((io.izzel.arclight.common.bridge.core.world.entity.monster.CreeperBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setIgnited(ignited);
    }
}
