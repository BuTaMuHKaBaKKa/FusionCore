package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.entity.CraftEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Fusion Core: Spigot's protected {@code CraftEntity#update} (resends the spawn packet), which Paper's entity API calls. */
@Mixin(value = CraftEntity.class, remap = false)
public interface CraftEntityAccessor {

    @Invoker("update")
    void fusioncore$update();
}
