package io.izzel.arclight.common.mixin.core.world.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.common.mod.util.FusionCorePaperPlayerEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's PlayerItemCooldownEvent (patch 0435) for a player's cooldowns ({@code ServerItemCooldowns} does not
 * override {@code addCooldown}, so the hook sits on the parent and only acts for server-side player cooldowns).
 */
@Mixin(ItemCooldowns.class)
public abstract class ItemCooldownsMixin {

    @WrapMethod(method = "addCooldown")
    private void fusioncore$cooldownEvent(Item item, int ticks, Operation<Void> original) {
        int cooldown = FusionCorePaperPlayerEvents.itemCooldown((ItemCooldowns) (Object) this, item, ticks);
        if (cooldown >= 0) {
            original.call(item, cooldown);
        }
    }
}
