package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.mod.util.FusionCoreMobs;
import io.papermc.paper.entity.SchoolableFish;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: the Bukkit types Paper declares as {@link SchoolableFish}, with the methods of Paper's
 * {@code PaperSchoolableFish} (patch 0557). See {@link RangedEntityTypesMixin} for why the mixin must keep its methods.
 */
@Mixin(value = {org.bukkit.entity.Cod.class, org.bukkit.entity.Salmon.class, org.bukkit.entity.TropicalFish.class}, remap = false)
public interface SchoolableFishTypesMixin extends SchoolableFish {

    @Override
    default void startFollowing(SchoolableFish leader) {
        FusionCoreMobs.startFollowing(this, leader);
    }

    @Override
    default void stopFollowing() {
        FusionCoreMobs.fish(this).stopFollowing();
    }

    @Override
    default int getSchoolSize() {
        return FusionCoreMobs.schoolSize(this);
    }

    @Override
    default int getMaxSchoolSize() {
        return FusionCoreMobs.fish(this).getMaxSchoolSize();
    }

    @Override
    default SchoolableFish getSchoolLeader() {
        return FusionCoreMobs.schoolLeader(this);
    }
}
