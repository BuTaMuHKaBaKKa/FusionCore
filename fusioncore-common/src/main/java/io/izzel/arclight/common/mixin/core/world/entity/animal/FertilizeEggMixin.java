package io.izzel.arclight.common.mixin.core.world.entity.animal;

import io.izzel.arclight.common.bridge.core.world.entity.animal.AnimalBridge;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fusion Core: Paper's EntityFertilizeEggEvent (patch 0795) for frogs and sniffers. */
@Mixin({Frog.class, Sniffer.class})
public abstract class FertilizeEggMixin {

    @Inject(method = "spawnChildFromBreeding", cancellable = true, at = @At("HEAD"))
    private void fusioncore$fertilizeEgg(ServerLevel level, Animal other, CallbackInfo ci) {
        boolean[] cancelled = new boolean[1];
        Animal self = (Animal) (Object) this;
        Integer experience = FusionCorePaperMobEvents.fertilizeEgg(self, other, ((AnimalBridge) self).bridge$getBreedItem(), cancelled);
        if (cancelled[0]) {
            ci.cancel();
        } else {
            FusionCorePaperMobEvents.setBreedExperience(experience);
        }
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("RETURN"))
    private void fusioncore$fertilizedEgg(ServerLevel level, Animal other, CallbackInfo ci) {
        FusionCorePaperMobEvents.setBreedExperience(null);
    }
}
