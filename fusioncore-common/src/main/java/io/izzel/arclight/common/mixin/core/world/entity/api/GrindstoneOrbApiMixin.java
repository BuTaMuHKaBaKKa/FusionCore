package io.izzel.arclight.common.mixin.core.world.entity.api;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.entity.ExperienceOrb$SpawnReason;
import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's GRINDSTONE orb reason (patch 0125). */
@Mixin(targets = "net.minecraft.world.inventory.GrindstoneMenu$4")
public abstract class GrindstoneOrbApiMixin {

    @WrapMethod(method = "onTake")
    private void fusioncore$grindstoneOrbs(Player player, ItemStack stack, Operation<Void> original) {
        FusionCoreExperienceOrbs.with(ExperienceOrb$SpawnReason.GRINDSTONE, player, null, () -> original.call(player, stack));
    }
}
