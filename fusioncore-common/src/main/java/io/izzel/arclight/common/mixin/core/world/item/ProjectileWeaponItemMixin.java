package io.izzel.arclight.common.mixin.core.world.item;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.bukkit.craftbukkit.v.event.CraftEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    @Decorate(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean arclight$shootBow(ServerLevel instance, Entity entity, ServerLevel serverLevel, LivingEntity livingEntity, InteractionHand interactionHand, ItemStack itemStack,
                                      List<ItemStack> list, float f, @Local(ordinal = -1) ItemStack itemstack1) throws Throwable {
        var event = CraftEventFactory.callEntityShootBowEvent(livingEntity, itemStack, itemstack1, entity, interactionHand, f, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            return (boolean) DecorationOps.cancel().invoke();
        }

        if (event.getProjectile() == entity.bridge$getBukkitEntity()) {
            if (!(boolean) DecorationOps.callsite().invoke(instance, entity)) {
                if (livingEntity instanceof net.minecraft.server.level.ServerPlayer) {
                    ((ServerPlayerBridge) livingEntity).bridge$getBukkitEntity().updateInventory();
                }
                return false;
            }
        }
        return true;
    }

    // Fusion Core: Paper's EntityLoadCrossbowEvent (patch 0459): a crossbow loaded without consuming the item takes ammo as if infinite
    @org.spongepowered.asm.mixin.injection.ModifyArg(method = "draw", index = 3, at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/item/ProjectileWeaponItem;useAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Z)Lnet/minecraft/world/item/ItemStack;"))
    private static boolean fusioncore$noAmmoUse(boolean intangible) {
        return intangible || io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents.crossbowNoConsume();
    }
}
