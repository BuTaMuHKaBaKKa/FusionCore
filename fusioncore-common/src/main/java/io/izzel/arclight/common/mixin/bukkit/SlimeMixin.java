package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Slime#canWander()} and {@code Slime#setWander(boolean)} (Paper patch 0232). */
@Mixin(value = org.bukkit.entity.Slime.class, remap = false)
public interface SlimeMixin {

    default boolean canWander() {
        return ((io.izzel.arclight.common.bridge.core.world.entity.monster.SlimeBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$canWander();
    }

    default void setWander(boolean canWander) {
        ((io.izzel.arclight.common.bridge.core.world.entity.monster.SlimeBridge) ((org.bukkit.craftbukkit.v.entity.CraftEntity) (Object) this).getHandle()).bridge$setWander(canWander);
    }
}
