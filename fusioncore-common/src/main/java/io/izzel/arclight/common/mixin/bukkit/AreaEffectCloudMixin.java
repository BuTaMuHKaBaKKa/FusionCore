package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's owner UUID (patch 0557).
 */
@Mixin(value = org.bukkit.entity.AreaEffectCloud.class, remap = false)
public interface AreaEffectCloudMixin {

    default java.util.UUID getOwnerUniqueId() {
        net.minecraft.world.entity.AreaEffectCloud h = FusionCoreEntities.nms(this);
        return h.ownerUUID;
    }

    default void setOwnerUniqueId(java.util.UUID ownerUuid) {
        net.minecraft.world.entity.AreaEffectCloud h = FusionCoreEntities.nms(this);
        h.setOwner(null);
        h.ownerUUID = ownerUuid;
    }
}
