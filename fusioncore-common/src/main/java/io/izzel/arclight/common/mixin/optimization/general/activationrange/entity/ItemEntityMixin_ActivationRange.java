package io.izzel.arclight.common.mixin.optimization.general.activationrange.entity;

import io.izzel.arclight.common.bridge.core.world.entity.item.ItemEntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mixin.optimization.general.activationrange.EntityMixin_ActivationRange;
import io.izzel.arclight.common.mod.ArclightConstants;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin_ActivationRange extends EntityMixin_ActivationRange implements ItemEntityBridge {

    // @formatter:off
    @Shadow public int pickupDelay;
    @Shadow public int age;
    @Shadow public abstract ItemStack getItem();
    // @formatter:on

    private int lastTick = ArclightConstants.currentTick - 1;

    @Override
    public void inactiveTick() {
        super.inactiveTick();
        int elapsedTicks = ArclightConstants.currentTick - this.lastTick;
        if (this.pickupDelay > 0 && this.pickupDelay != 32767 && elapsedTicks > 0) this.pickupDelay -= elapsedTicks;
        if (this.age != -32768) this.age += elapsedTicks;
        this.lastTick = ArclightConstants.currentTick;
        this.bridge$forge$optimization$discardItemEntity();
    }

    @Override
    public int bridge$fusioncore$despawnAge() {
        return ((WorldBridge) this.level()).bridge$spigotConfig().itemDespawnRate;
    }

    // Fusion Core: the display copy /give drops (makeFakeItem) despawns on the next tick, as Paper's despawnRate - 1;
    // vanilla's 5999 kept it on the ground (visible) until item-despawn-rate when that is above 6000
    @Inject(method = "makeFakeItem", at = @At("RETURN"))
    private void fusioncore$fakeItemDespawns(CallbackInfo ci) {
        this.age = this.bridge$fusioncore$despawnAge() - 1;
    }

    @Override
    public void bridge$forge$optimization$discardItemEntity() {
        if (!this.level().isClientSide && this.age >= ((WorldBridge) this.level()).bridge$spigotConfig().itemDespawnRate) {
            this.bridge$pushEntityRemoveCause(EntityRemoveEvent.Cause.DEATH);
            this.discard();
        }
    }
}
