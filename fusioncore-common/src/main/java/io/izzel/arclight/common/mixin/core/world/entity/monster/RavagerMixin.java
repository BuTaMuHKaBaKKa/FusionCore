package io.izzel.arclight.common.mixin.core.world.entity.monster;

import io.izzel.arclight.common.mixin.core.world.entity.PathfinderMobMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Ravager.class)
public abstract class RavagerMixin extends PathfinderMobMixin {

    @Redirect(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$entityChangeBlock(Level world, BlockPos pos, boolean dropBlock, Entity entityIn) {
        return CraftEventFactory.callEntityChangeBlockEvent((Ravager) (Object) this, pos, world.getFluidState(pos).createLegacyBlock()) // Fusion Core: Paper 0683
           
            && world.destroyBlock(pos, dropBlock, entityIn);
    }

    // Fusion Core: Paper's EntityJumpEvent (Paper patch 0316)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Ravager;jumpFromGround()V"))
    private void fusioncore$jumpEvent(net.minecraft.world.entity.monster.Ravager ravager, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (io.izzel.arclight.common.mod.util.FusionCoreEntities.jumpEvent(ravager)) {
            original.call(ravager);
        }
    }

    // Fusion Core: Paper's EntityPushedByEntityAttackEvent (Paper patch 0202)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "strongKnockback", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;push(DDD)V"))
    private void fusioncore$pushedBy(net.minecraft.world.entity.Entity pushed, double x, double y, double z,
                                   com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        io.izzel.arclight.common.mod.util.FusionCoreEntities.pushedBy(pushed, (net.minecraft.world.entity.Entity) (Object) this, x, y, z, original);
    }
}
