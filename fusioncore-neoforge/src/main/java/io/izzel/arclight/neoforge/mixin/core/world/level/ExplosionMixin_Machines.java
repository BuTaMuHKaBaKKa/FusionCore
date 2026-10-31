package io.izzel.arclight.neoforge.mixin.core.world.level;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;

// Fusion Core: blocks broken by an explosion are not a machine's own block changes; the explosion fires its explode events
@Mixin(Explosion.class)
public class ExplosionMixin_Machines {

    @WrapMethod(method = "finalizeExplosion")
    private void fusioncore$notMachine(boolean spawnParticles, Operation<Void> original) {
        if (!MachineOwners.hasContext()) {
            original.call(spawnParticles);
            return;
        }
        MachineOwners.suspend();
        try {
            original.call(spawnParticles);
        } finally {
            MachineOwners.resume();
        }
    }
}
