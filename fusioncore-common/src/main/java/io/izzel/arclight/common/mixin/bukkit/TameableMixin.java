package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's {@code Tameable#getOwnerUniqueId} (patch 0171). */
@Mixin(value = org.bukkit.entity.Tameable.class, remap = false)
public interface TameableMixin {

    default java.util.UUID getOwnerUniqueId() {
        return io.izzel.arclight.common.mod.util.FusionCoreEntities.ownerUniqueId((org.bukkit.entity.Tameable) this);
    }
}
