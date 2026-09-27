package io.izzel.arclight.common.mixin.bukkit.inventory;

import org.bukkit.craftbukkit.v.inventory.CraftMetaSpawnEgg;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: Paper's custom spawned type of a spawn egg (patch 0803); mod entity ids resolve too. */
@Mixin(value = CraftMetaSpawnEgg.class, remap = false)
public abstract class CraftMetaSpawnEggMixin {

    @Shadow private net.minecraft.nbt.CompoundTag entityTag;

    public org.bukkit.entity.EntityType getCustomSpawnedType() {
        if (this.entityTag == null || !this.entityTag.contains("id", net.minecraft.nbt.Tag.TAG_STRING)) {
            return null;
        }
        org.bukkit.NamespacedKey key = org.bukkit.NamespacedKey.fromString(this.entityTag.getString("id"));
        return key == null ? null : org.bukkit.Registry.ENTITY_TYPE.get(key);
    }

    public void setCustomSpawnedType(org.bukkit.entity.EntityType type) {
        if (type == null) {
            if (this.entityTag != null) {
                this.entityTag.remove("id");
            }
        } else {
            if (this.entityTag == null) {
                this.entityTag = new net.minecraft.nbt.CompoundTag();
            }
            this.entityTag.putString("id", type.getKey().toString());
        }
    }
}
