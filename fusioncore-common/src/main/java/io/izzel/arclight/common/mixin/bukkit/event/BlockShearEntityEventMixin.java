package io.izzel.arclight.common.mixin.bukkit.event;

import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import org.bukkit.event.block.BlockShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

/**
 * Fusion Core: Paper's drops of {@code BlockShearEntityEvent} (Paper patch 0916). Given by {@code FusionCoreEvents.newBlockShearEntityEvent} (the core builds it
 * with the entity's default drops, see {@code FusionCoreShearing}); empty with Spigot's constructors, as with Paper's.
 */
@Mixin(value = BlockShearEntityEvent.class, remap = false)
public abstract class BlockShearEntityEventMixin implements io.izzel.arclight.common.bridge.bukkit.ShearDropsBridge {

    private List<ItemStack> fusioncore$drops = Collections.emptyList();

    @Inject(method = "<init>", at = @At("RETURN"))
    @SuppressWarnings("unchecked")
    private void fusioncore$takeDrops(CallbackInfo ci) {
        List<ItemStack> drops = FusionCoreEvents.takeConstructorArgument(List.class);
        if (drops != null) {
            this.fusioncore$drops = drops;
        }
    }

    public List<ItemStack> getDrops() {
        return Collections.unmodifiableList(this.fusioncore$drops);
    }

    @Override
    public List<ItemStack> fusioncore$getDrops() {
        return this.fusioncore$drops;
    }

    public void setDrops(List<ItemStack> drops) {
        this.fusioncore$drops = List.copyOf(drops);
    }
}
