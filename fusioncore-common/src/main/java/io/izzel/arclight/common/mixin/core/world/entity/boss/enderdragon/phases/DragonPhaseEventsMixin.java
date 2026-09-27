package io.izzel.arclight.common.mixin.core.world.entity.boss.enderdragon.phases;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonSittingFlamingPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import net.minecraft.world.entity.projectile.DragonFireball;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fusion Core: Paper's EnderDragonFlameEvent and EnderDragonShootFireballEvent (patch 0211). A cancelled flame ends the
 * sitting-flaming phase; a cancelled fireball is not spawned.
 */
@Mixin({DragonSittingFlamingPhase.class, DragonStrafePlayerPhase.class})
public abstract class DragonPhaseEventsMixin extends AbstractDragonPhaseInstance {

    private DragonPhaseEventsMixin(EnderDragon dragon) {
        super(dragon);
    }

    @WrapOperation(method = "doServerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$dragonEvents(Level level, Entity entity, Operation<Boolean> original) {
        if (entity instanceof AreaEffectCloud && !FusionCorePaperMobEvents.dragonFlame(this.dragon, entity)) {
            this.end();
            return false;
        }
        if (entity instanceof DragonFireball && !FusionCorePaperMobEvents.dragonShootFireball(this.dragon, entity)) {
            return false;
        }
        return original.call(level, entity);
    }
}
