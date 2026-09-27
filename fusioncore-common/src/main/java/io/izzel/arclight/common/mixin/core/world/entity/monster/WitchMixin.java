package io.izzel.arclight.common.mixin.core.world.entity.monster;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.bridge.core.world.entity.monster.WitchBridge;
import io.izzel.arclight.common.mixin.core.world.entity.raid.RaiderMixin;
import io.izzel.arclight.common.mod.util.FusionCorePaperEntityEvents;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(Witch.class)
public abstract class WitchMixin extends RaiderMixin implements WitchBridge {

    // @formatter:off
    @Shadow private int usingTime;
    @Shadow @Final private static ResourceLocation SPEED_MODIFIER_DRINKING_ID;
    @Shadow @Final private static AttributeModifier SPEED_MODIFIER_DRINKING;
    // @formatter:on

    /** Fusion Core: the potion WitchThrowPotionEvent left to throw, handed from the event to the projectile. */
    @Unique private ItemStack fusioncore$thrownPotion;

    @Decorate(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;forEachEffect(Ljava/util/function/Consumer;)V"))
    private void arclight$reason(PotionContents instance, Consumer<MobEffectInstance> consumer) throws Throwable {
        Consumer<MobEffectInstance> wrapped = effect -> {
            bridge$pushEffectCause(EntityPotionEffectEvent.Cause.ATTACK);
            consumer.accept(effect);
        };
        DecorationOps.callsite().invoke(instance, wrapped);
    }

    // Fusion Core start: Paper's witch potion events (Paper patches 0190-0192) and Witch API (0256)

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"))
    private Object fusioncore$consumePotion(ItemStack stack, DataComponentType<?> type, Operation<Object> original) {
        return FusionCorePaperEntityEvents.witchConsume((Witch) (Object) this, stack, (PotionContents) original.call(stack, type));
    }

    @WrapOperation(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack fusioncore$readyPotion(Item item, Holder<Potion> potion, Operation<ItemStack> original) {
        return FusionCorePaperEntityEvents.witchReady((Witch) (Object) this, original.call(item, potion));
    }

    @Inject(method = "performRangedAttack", cancellable = true, at = @At(value = "NEW", target = "net/minecraft/world/entity/projectile/ThrownPotion"))
    private void fusioncore$throwPotion(LivingEntity target, float velocity, CallbackInfo ci, @Local Holder<Potion> potion) {
        ItemStack stack = FusionCorePaperEntityEvents.witchThrow((Witch) (Object) this, target, PotionContents.createItemStack(Items.SPLASH_POTION, potion));
        if (stack == null) {
            ci.cancel();
        } else {
            this.fusioncore$thrownPotion = stack;
        }
    }

    @WrapOperation(method = "performRangedAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/alchemy/PotionContents;createItemStack(Lnet/minecraft/world/item/Item;Lnet/minecraft/core/Holder;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack fusioncore$thrownPotion(Item item, Holder<Potion> potion, Operation<ItemStack> original) {
        ItemStack stack = this.fusioncore$thrownPotion;
        this.fusioncore$thrownPotion = null;
        return stack != null ? stack : original.call(item, potion);
    }

    @Override
    public int bridge$getUsingTime() {
        return this.usingTime;
    }

    @Override
    public void bridge$setUsingTime(int usingTime) {
        this.usingTime = usingTime;
    }

    @Override
    public void bridge$setDrinkingPotion(ItemStack potion) {
        Witch self = (Witch) (Object) this;
        potion = FusionCorePaperEntityEvents.witchReady(self, potion);
        self.setItemSlot(EquipmentSlot.MAINHAND, potion);
        this.usingTime = self.getMainHandItem().getUseDuration(self);
        self.setUsingItem(true);
        if (!self.isSilent()) {
            self.level().playSound(null, self.getX(), self.getY(), self.getZ(), SoundEvents.WITCH_DRINK, self.getSoundSource(), 1.0F, 0.8F + self.getRandom().nextFloat() * 0.4F);
        }
        AttributeInstance speed = self.getAttribute(Attributes.MOVEMENT_SPEED);
        speed.removeModifier(SPEED_MODIFIER_DRINKING_ID);
        speed.addTransientModifier(SPEED_MODIFIER_DRINKING);
    }
    // Fusion Core end
}
