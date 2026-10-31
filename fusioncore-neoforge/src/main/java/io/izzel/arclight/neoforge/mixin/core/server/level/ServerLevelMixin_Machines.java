package io.izzel.arclight.neoforge.mixin.core.server.level;

import io.izzel.arclight.neoforge.mod.machines.MachineOwners;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Fusion Core: an entity of a mod created by a machine (Create's contraption from a bearing) inherits the machine's owner
@Mixin(ServerLevel.class)
public class ServerLevelMixin_Machines {

    @Inject(method = "addFreshEntity", at = @At("HEAD"))
    private void fusioncore$inheritOwner(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (MachineOwners.hasContext() && MachineOwners.machineModOf(entity) != null) {
            MachineOwners.inherit(entity.getPersistentData());
        }
    }
}
