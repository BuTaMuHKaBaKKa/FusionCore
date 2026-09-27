package io.izzel.arclight.common.mixin.bukkit;

import org.bukkit.craftbukkit.v.attribute.CraftAttributeInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: the vanilla attribute behind a Bukkit one, for Paper's addTransientModifier. */
@Mixin(value = CraftAttributeInstance.class, remap = false)
public interface CraftAttributeInstanceAccessor {

    @Accessor("handle")
    net.minecraft.world.entity.ai.attributes.AttributeInstance fusioncore$handle();
}
