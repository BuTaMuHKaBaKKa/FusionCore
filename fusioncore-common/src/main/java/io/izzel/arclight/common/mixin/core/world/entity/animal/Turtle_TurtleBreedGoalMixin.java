package io.izzel.arclight.common.mixin.core.world.entity.animal;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.bridge.core.world.entity.animal.AnimalBridge;
import io.izzel.arclight.common.mod.util.FusionCorePaperMobEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fusion Core: Paper's EntityFertilizeEggEvent (patch 0795) for turtles: cancelled, the turtle gets no egg and both lose their
 * love; otherwise the event's experience drops (none when it is 0).
 */
@Mixin(targets = "net.minecraft.world.entity.animal.Turtle$TurtleBreedGoal")
public abstract class Turtle_TurtleBreedGoalMixin extends BreedGoal {

    @Unique
    private Integer fusioncore$eggExperience;

    private Turtle_TurtleBreedGoalMixin(Animal animal, double speed) {
        super(animal, speed);
    }

    @Inject(method = "breed", cancellable = true, at = @At("HEAD"))
    private void fusioncore$fertilizeEgg(CallbackInfo ci) {
        this.fusioncore$eggExperience = null;
        if (this.partner == null) {
            return;
        }
        boolean[] cancelled = new boolean[1];
        this.fusioncore$eggExperience = FusionCorePaperMobEvents.fertilizeEgg(this.animal, this.partner, ((AnimalBridge) this.animal).bridge$getBreedItem(), cancelled);
        if (cancelled[0]) {
            ci.cancel();
        }
    }

    @ModifyArg(method = "breed", index = 4, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ExperienceOrb;<init>(Lnet/minecraft/world/level/Level;DDDI)V"))
    private int fusioncore$eggExperience(int experience) {
        return this.fusioncore$eggExperience != null ? this.fusioncore$eggExperience : experience;
    }

    @WrapOperation(method = "breed", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean fusioncore$noEggExperience(Level level, Entity orb, Operation<Boolean> original) {
        Integer experience = this.fusioncore$eggExperience;
        this.fusioncore$eggExperience = null;
        return (experience == null || experience > 0) && original.call(level, orb);
    }
}
