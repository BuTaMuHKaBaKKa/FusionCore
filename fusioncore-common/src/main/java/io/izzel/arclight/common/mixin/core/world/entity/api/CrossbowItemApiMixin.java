package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.izzel.arclight.common.bridge.core.world.entity.api.FireworkApiBridge;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Fusion Core: Paper records the shooter of a crossbow firework as its spawning entity (patch 0121). */
@Mixin(CrossbowItem.class)
public abstract class CrossbowItemApiMixin {

    @ModifyReturnValue(method = "createProjectile", at = @At("RETURN"))
    private Projectile fusioncore$fireworkSpawner(Projectile projectile, Level level, LivingEntity shooter, ItemStack weapon, ItemStack ammo, boolean isCrit) {
        if (projectile instanceof FireworkRocketEntity) {
            ((FireworkApiBridge) projectile).fusioncore$setSpawningEntity(shooter.getUUID());
        }
        return projectile;
    }
}
