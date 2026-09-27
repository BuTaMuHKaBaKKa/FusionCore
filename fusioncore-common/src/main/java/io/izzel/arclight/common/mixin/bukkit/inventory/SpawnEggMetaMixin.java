package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's custom spawned type (patch 0803 "Fix SpawnEggMeta get/setSpawnedType"). The only implementation (CraftMetaSpawnEgg) overrides these through
 * {@code CraftMetaSpawnEggMixin}; the defaults only exist so that the plugin's {@code INVOKEINTERFACE} resolves.
 */
@Mixin(value = org.bukkit.inventory.meta.SpawnEggMeta.class, remap = false)
public interface SpawnEggMetaMixin {

    default org.bukkit.entity.EntityType getCustomSpawnedType() {
        throw new UnsupportedOperationException(this.getClass().getName());
    }

    default void setCustomSpawnedType(org.bukkit.entity.EntityType type) {
        throw new UnsupportedOperationException(this.getClass().getName());
    }
}
