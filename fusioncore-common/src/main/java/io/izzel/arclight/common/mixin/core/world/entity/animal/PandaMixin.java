package io.izzel.arclight.common.mixin.core.world.entity.animal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(Panda.class)
public abstract class PandaMixin extends AnimalMixin {

    @Shadow @Final static Predicate<ItemEntity> PANDA_ITEMS;

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    protected void pickUpItem(ItemEntity itemEntity) {
        boolean cancel = this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && PANDA_ITEMS.test(itemEntity);
        if (!CraftEventFactory.callEntityPickupItemEvent((Panda) (Object) this, itemEntity, 0, cancel).isCancelled()) {
            ItemStack itemstack = itemEntity.getItem();
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.handDropChances[EquipmentSlot.MAINHAND.getIndex()] = 2.0F;
            this.take(itemEntity, itemstack.getCount());
            this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.PICKUP);
            itemEntity.discard();
        }

    }

    // Fusion Core: Paper's EntityJumpEvent (Paper patch 0316) for pandas jumping at a sneeze; the event is for the panda that
    // jumps (Paper passes the sneezing one)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(method = "afterSneeze", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Panda;jumpFromGround()V"))
    private void fusioncore$jumpEvent(net.minecraft.world.entity.animal.Panda panda, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        if (io.izzel.arclight.common.mod.util.FusionCoreEntities.jumpEvent(panda)) {
            original.call(panda);
        }
    }

    // Fusion Core: Paper's EntityToggleSitEvent (patch 0755)
    @org.spongepowered.asm.mixin.injection.Inject(method = "sit", cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$sitEvent(boolean sitting, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (true && !io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.toggleSit((net.minecraft.world.entity.Entity) (Object) this, sitting)) {
            ci.cancel();
        }
    }
}
