package io.izzel.arclight.common.mixin.core.world.entity.decoration;

import io.izzel.arclight.common.mixin.core.world.entity.decoration.BlockAttachedEntityMixin;
import io.izzel.arclight.common.mod.mixins.annotation.TransformAccess;
import io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin extends BlockAttachedEntityMixin {

    // @formatter:off
    @Shadow @Final private static EntityDataAccessor<ItemStack> DATA_ITEM;
    @Shadow protected abstract void onItemChanged(ItemStack p_218866_);
    @Shadow public abstract ItemStack getItem();
    @Shadow public abstract void setItem(ItemStack stack, boolean updateNeighbours);
    // @formatter:on

    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;dropItem(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void arclight$damageNonLiving(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (CraftEventFactory.handleNonLivingEntityDamageEvent((ItemFrame) (Object) this, source, amount, false) || this.isRemoved()) {
            cir.setReturnValue(true);
        }
    }

    // Fusion Core: Paper's PlayerItemFrameChangeEvent (patch 0631): REMOVE when a player knocks the item out, PLACE and ROTATE
    // from interact. Cancelling REMOVE keeps the item but not the damage.
    @Inject(method = "hurt", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;dropItem(Lnet/minecraft/world/entity/Entity;Z)V"))
    private void fusioncore$removeItemEvent(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            ItemStack item = FusionCorePaperPlayerEvents.itemFrameChange(player, (ItemFrame) (Object) this, this.getItem(), "REMOVE");
            if (item == null) {
                cir.setReturnValue(true);
                return;
            }
            this.setItem(item, false);
        }
    }

    @Inject(method = "interact", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void fusioncore$placeItemEvent(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        ItemStack item = FusionCorePaperPlayerEvents.itemFrameChange(player, (ItemFrame) (Object) this, player.getItemInHand(hand), "PLACE");
        if (item == null) {
            cir.setReturnValue(net.minecraft.world.InteractionResult.FAIL);
            return;
        }
        this.fusioncore$placed = item;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "interact", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;setItem(Lnet/minecraft/world/item/ItemStack;)V"))
    private void fusioncore$placedItem(ItemFrame frame, ItemStack stack, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        ItemStack placed = this.fusioncore$placed;
        this.fusioncore$placed = null;
        original.call(frame, placed != null ? placed : stack);
    }

    @Inject(method = "interact", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/decoration/ItemFrame;getRotateItemSound()Lnet/minecraft/sounds/SoundEvent;"))
    private void fusioncore$rotateItemEvent(net.minecraft.world.entity.player.Player player, net.minecraft.world.InteractionHand hand, CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        ItemStack item = FusionCorePaperPlayerEvents.itemFrameChange(player, (ItemFrame) (Object) this, this.getItem(), "ROTATE");
        if (item == null) {
            cir.setReturnValue(net.minecraft.world.InteractionResult.FAIL);
            return;
        }
        if (item != this.getItem()) {
            this.setItem(item, false, false);
        }
    }

    private transient ItemStack fusioncore$placed;

    public void setItem(ItemStack itemstack, final boolean flag, final boolean playSound) {
        if (!itemstack.isEmpty()) {
            itemstack = itemstack.copy();
            itemstack.setCount(1);
        }
        this.onItemChanged(itemstack);
        this.getEntityData().set(DATA_ITEM, itemstack);
        if (!itemstack.isEmpty() && playSound) {
            this.playSound(SoundEvents.ITEM_FRAME_ADD_ITEM, 1.0f, 1.0f);
        }
        if (flag && this.pos != null) {
            this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
        }
    }

    @Inject(method = "calculateBoundingBox", at = @At("HEAD"), cancellable = true)
    private void arclight$calculateBoundingBox(BlockPos blockPos, Direction direction, CallbackInfoReturnable<AABB> cir) {
        cir.setReturnValue(calculateBoundingBoxStatic(blockPos, direction));
    }

    @TransformAccess(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)
    private static AABB calculateBoundingBoxStatic(BlockPos blockPos, Direction direction) {
        float f = 0.46875F;
        Vec3 vec3d = Vec3.atCenterOf(blockPos).relative(direction, -0.46875D);
        Direction.Axis axis = direction.getAxis();
        double d0 = axis == Direction.Axis.X ? 0.0625D : 0.75D;
        double d1 = axis == Direction.Axis.Y ? 0.0625D : 0.75D;
        double d2 = axis == Direction.Axis.Z ? 0.0625D : 0.75D;
        return AABB.ofSize(vec3d, d0, d1, d2);
    }
}
