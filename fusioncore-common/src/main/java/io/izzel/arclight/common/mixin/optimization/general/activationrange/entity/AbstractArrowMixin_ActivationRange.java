package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import io.izzel.arclight.common.mixin.optimization.general.activationrange.EntityMixin_ActivationRange;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin_ActivationRange extends EntityMixin_ActivationRange {

    // @formatter:off
    @Shadow public boolean inGround;
    @Shadow public int life;
    // @formatter:on

    @Override
    public void inactiveTick() {
        super.inactiveTick();
        // Fusion Core: Spigot counts the despawn timer of an inactive arrow in the ground (was inGroundTime, so arrows
        // away from players never despawned)
        if (this.inGround) {
            this.life++;
        }
    }
}
