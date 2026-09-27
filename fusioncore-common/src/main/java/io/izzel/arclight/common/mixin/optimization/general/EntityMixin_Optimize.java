package io.izzel.arclight.common.mixin.optimization.general;

import com.google.common.collect.ImmutableList;
import io.izzel.arclight.common.mod.compat.ModIds;
import io.izzel.arclight.common.mod.mixins.annotation.LoadIfMod;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

@Mixin(Entity.class)
// Fusion Core: Lithium's alloc.deep_passengers @Overwrites getIndirectPassengers and
// getIndirectPassengersStream, the same methods this mixin overwrites and injects into.
// Its version is equivalent, so let the Lithium family own them.
@LoadIfMod(modid = {ModIds.LITHIUM, ModIds.CANARY, ModIds.RADIUM}, condition = LoadIfMod.ModCondition.ABSENT)
public abstract class EntityMixin_Optimize {

    @Shadow public ImmutableList<Entity> passengers;

    @Inject(method = "getIndirectPassengersStream", cancellable = true, at = @At("HEAD"))
    private void arclight$emptyPassenger(CallbackInfoReturnable<Stream<Entity>> cir) {
        if (this.passengers.isEmpty()) {
            cir.setReturnValue(Stream.empty());
        }
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite
    public Iterable<Entity> getIndirectPassengers() {
        if (this.passengers.isEmpty()) {
            return Collections.emptyList();
        } else {
            var list = new ArrayList<Entity>();
            for (var entity : this.passengers) {
                list.add(entity);
                // Fusion Core: Minecraft's contract here is Iterable; another mod need not return a Collection.
                for (var passenger : entity.getIndirectPassengers()) {
                    list.add(passenger);
                }
            }
            return list;
        }
    }
}
