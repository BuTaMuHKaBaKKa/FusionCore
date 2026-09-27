package io.izzel.arclight.common.mixin.core.world.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.DoorInteractGoal;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BreakDoorGoal.class)
public abstract class BreakDoorGoalMixin extends DoorInteractGoal {

    public BreakDoorGoalMixin(Mob entityIn) {
        super(entityIn);
    }

    @Inject(method = "tick", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private void arclight$breakDoor(CallbackInfo ci) {
        // Fusion Core: Paper's new block data, the fluid left behind (patch 0683)
        var event = io.izzel.arclight.common.mod.util.FusionCoreEvents.newEntityBreakDoorEvent((org.bukkit.entity.LivingEntity) this.mob.bridge$getBukkitEntity(),
            org.bukkit.craftbukkit.v.block.CraftBlock.at(this.mob.level(), this.doorPos),
            org.bukkit.craftbukkit.v.block.data.CraftBlockData.fromData(this.mob.level().getFluidState(this.doorPos).createLegacyBlock()));
        org.bukkit.Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            this.start();
            ci.cancel();
        }
    }
}
