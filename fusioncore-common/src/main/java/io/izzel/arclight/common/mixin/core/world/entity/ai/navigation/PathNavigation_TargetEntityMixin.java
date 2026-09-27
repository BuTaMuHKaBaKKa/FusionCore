package io.izzel.arclight.common.mixin.core.world.entity.ai.navigation;

import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.level.pathfinder.Path;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fusion Core: remembers the entity a path is made to, for Paper's EntityPathfindEvent (every vanilla override). */
@Mixin({PathNavigation.class, GroundPathNavigation.class, FlyingPathNavigation.class, WallClimberNavigation.class})
public abstract class PathNavigation_TargetEntityMixin {

    @Inject(method = "createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("HEAD"))
    private void fusioncore$pathTarget(Entity entity, int accuracy, CallbackInfoReturnable<Path> cir) {
        FusionCorePaperEntityEvents.pathTarget(entity);
    }

    @Inject(method = "createPath(Lnet/minecraft/world/entity/Entity;I)Lnet/minecraft/world/level/pathfinder/Path;", at = @At("RETURN"))
    private void fusioncore$pathTargetDone(Entity entity, int accuracy, CallbackInfoReturnable<Path> cir) {
        FusionCorePaperEntityEvents.pathTarget(null);
    }
}
