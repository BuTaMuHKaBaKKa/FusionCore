package io.izzel.arclight.common.mixin.core.world.entity.animal;

import net.minecraft.world.entity.animal.AbstractSchoolingFish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Fusion Core: school state Paper's {@code SchoolableFish} API reads (patch 0557). */
@Mixin(AbstractSchoolingFish.class)
public interface AbstractSchoolingFishAccessor {

    @Accessor("leader")
    AbstractSchoolingFish fusioncore$leader();

    @Accessor("schoolSize")
    int fusioncore$schoolSize();
}
