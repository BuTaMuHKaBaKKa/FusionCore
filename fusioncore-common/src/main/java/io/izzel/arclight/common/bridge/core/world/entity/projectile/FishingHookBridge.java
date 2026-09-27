package io.izzel.arclight.common.bridge.core.world.entity.projectile;

import io.izzel.tools.product.Product;
import io.izzel.tools.product.Product2;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface FishingHookBridge {

    default Product2<Boolean, Integer> bridge$forge$onItemFished(List<ItemStack> stacks, int rodDamage, FishingHook hook) {
        return Product.of(false, rodDamage);
    }

    /** Fusion Core: Paper's {@code FishingHook#resetTimeUntilLured} (patch 0674), with Spigot's wait times and lure flag. */
    void fusioncore$resetTimeUntilLured();

    /** Fusion Core: a new fish angle within Spigot's lure angles, as when the hook enters the lure stage. */
    void fusioncore$randomFishAngle();
}
