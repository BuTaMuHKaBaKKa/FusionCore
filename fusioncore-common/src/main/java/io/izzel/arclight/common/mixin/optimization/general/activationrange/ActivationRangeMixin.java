package io.izzel.arclight.common.mixin.optimization.general.activationrange;

import io.izzel.arclight.common.bridge.optimization.EntityBridge_ActivationRange;
import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

// Fusion Core: Spigot's ActivationRange hands over to Paper's Entity Activation Range 2.0 (FusionCoreActivation), which also
// computes Pufferfish's DAB priority
@Mixin(value = ActivationRange.class, remap = false)
public class ActivationRangeMixin {

    /**
     * @author IzzelAliz
     * @reason entityLists
     */
    @Overwrite
    private static void activateEntity(Entity entity) {
        ((EntityBridge_ActivationRange) entity).bridge$updateActivation();
    }

    /**
     * @author Fusion Core
     * @reason Paper's Entity Activation Range 2.0
     */
    @Overwrite
    public static void activateEntities(Level level) {
        FusionCoreActivation.activateEntities(level);
    }

    /**
     * @author Fusion Core
     * @reason Paper's Entity Activation Range 2.0
     */
    @Overwrite
    public static boolean checkIfActive(Entity entity) {
        return FusionCoreActivation.checkIfActive(entity);
    }

    /**
     * @author Fusion Core
     * @reason Paper's Entity Activation Range 2.0 (immunities have a length; this answers whether there is one)
     */
    @Overwrite
    public static boolean checkEntityImmunities(Entity entity) {
        return FusionCoreActivation.immunity(entity) >= 0;
    }
}
