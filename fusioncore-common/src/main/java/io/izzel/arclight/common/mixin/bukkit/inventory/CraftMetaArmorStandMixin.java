package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;
import org.bukkit.craftbukkit.v.inventory.CraftMetaArmorStand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

/** Fusion Core: Paper's {@link ArmorStandMeta} (patch 0169), stored in the item's entity data. */
@Mixin(value = CraftMetaArmorStand.class, remap = false)
public abstract class CraftMetaArmorStandMixin implements ArmorStandMeta {

    // @formatter:off
    @Shadow net.minecraft.nbt.CompoundTag entityTag;
    @Override @Shadow public abstract CraftMetaArmorStand clone();
    // @formatter:on

    @Unique
    private void fusioncore$populateTagIfNull() {
        if (this.entityTag == null) {
            this.entityTag = new net.minecraft.nbt.CompoundTag();
            this.entityTag.putString("id", "minecraft:armor_stand");
        }
    }

    @Unique
    private boolean fusioncore$flag(String key) {
        return this.entityTag != null && this.entityTag.contains(key) && this.entityTag.getBoolean(key);
    }

    @Unique
    private void fusioncore$setFlag(String key, boolean value) {
        this.fusioncore$populateTagIfNull();
        this.entityTag.putBoolean(key, value);
    }

    @Override
    public boolean isInvisible() {
        return this.fusioncore$flag("Invisible");
    }

    @Override
    public boolean hasNoBasePlate() {
        return this.fusioncore$flag("NoBasePlate");
    }

    @Override
    public boolean shouldShowArms() {
        return this.fusioncore$flag("ShowArms");
    }

    @Override
    public boolean isSmall() {
        return this.fusioncore$flag("Small");
    }

    @Override
    public boolean isMarker() {
        return this.fusioncore$flag("Marker");
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.fusioncore$setFlag("Invisible", invisible);
    }

    @Override
    public void setNoBasePlate(boolean noBasePlate) {
        this.fusioncore$setFlag("NoBasePlate", noBasePlate);
    }

    @Override
    public void setShowArms(boolean showArms) {
        this.fusioncore$setFlag("ShowArms", showArms);
    }

    @Override
    public void setSmall(boolean small) {
        this.fusioncore$setFlag("Small", small);
    }

    @Override
    public void setMarker(boolean marker) {
        this.fusioncore$setFlag("Marker", marker);
    }
}
