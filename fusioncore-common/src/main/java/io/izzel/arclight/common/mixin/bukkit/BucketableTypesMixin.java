package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreEntities;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: the Bukkit types Paper declares as {@link io.papermc.paper.entity.Bucketable}, with {@code PaperBucketable}'s bodies (patch 0643). */
@Mixin(value = {org.bukkit.entity.Axolotl.class, org.bukkit.entity.Fish.class}, remap = false)
public interface BucketableTypesMixin extends io.papermc.paper.entity.Bucketable {

    @Override
    default boolean isFromBucket() {
        return FusionCoreEntities.bucketable(this).fromBucket();
    }

    @Override
    default void setFromBucket(boolean fromBucket) {
        FusionCoreEntities.bucketable(this).setFromBucket(fromBucket);
    }

    @Override
    default org.bukkit.inventory.ItemStack getBaseBucketItem() {
        return FusionCoreEntities.baseBucketItem(this);
    }

    @Override
    default org.bukkit.Sound getPickupSound() {
        return FusionCoreEntities.bucketPickupSound(this);
    }
}
