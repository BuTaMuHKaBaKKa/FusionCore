package io.izzel.arclight.common.mixin.core.world.entity.ai.navigation;

import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

/** Fusion Core: Paper's EntityPathfindEvent (patch 0080); refused targets are skipped. */
@Mixin(PathNavigation.class)
public abstract class PathNavigationMixin {

    @Shadow @Final protected Mob mob;

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "createPath(Ljava/util/Set;IZIF)Lnet/minecraft/world/level/pathfinder/Path;",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/PathFinder;findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;"))
    private Path fusioncore$pathfindEvent(PathFinder finder, PathNavigationRegion region, Mob mob, Set<BlockPos> targets, float maxRange, int accuracy, float searchDepth,
                                       com.llamalad7.mixinextras.injector.wrapoperation.Operation<Path> original) {
        Set<BlockPos> allowed = FusionCorePaperEntityEvents.pathfind(this.mob, targets);
        return allowed.isEmpty() ? null : original.call(finder, region, mob, allowed, maxRange, accuracy, searchDepth);
    }
}
