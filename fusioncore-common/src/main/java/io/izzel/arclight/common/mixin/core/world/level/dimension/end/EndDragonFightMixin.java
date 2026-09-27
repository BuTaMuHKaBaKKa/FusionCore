package io.izzel.arclight.common.mixin.core.world.level.dimension.end;

import io.izzel.arclight.common.mod.util.FusionCorePaperBlockEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's DragonEggFormEvent (patch 0483); the event places the egg when someone listens. */
@Mixin(EndDragonFight.class)
public abstract class EndDragonFightMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel level;
    @Shadow private BlockPos origin;
    @Shadow private boolean previouslyKilled;
    // @formatter:on

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "setDragonKilled", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/server/level/ServerLevel;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean fusioncore$eggByEvent(ServerLevel level, BlockPos pos, BlockState state, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Boolean> original) {
        return FusionCorePaperBlockEvents.dragonEggFormListened() || original.call(level, pos, state);
    }

    @Inject(method = "setDragonKilled", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
        target = "Lnet/minecraft/world/level/dimension/end/EndDragonFight;previouslyKilled:Z"))
    private void fusioncore$dragonEggForm(net.minecraft.world.entity.boss.enderdragon.EnderDragon dragon, CallbackInfo ci) {
        if (FusionCorePaperBlockEvents.dragonEggFormListened()) {
            FusionCorePaperBlockEvents.dragonEggForm((EndDragonFight) (Object) this, this.level, this.origin, this.previouslyKilled);
        }
    }
}
