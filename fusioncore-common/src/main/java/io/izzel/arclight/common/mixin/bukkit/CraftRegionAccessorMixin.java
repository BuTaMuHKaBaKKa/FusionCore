package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.world.entity.EntityTypeBridge;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.CraftRegionAccessor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Function;

@Mixin(value = CraftRegionAccessor.class, remap = false)
public abstract class CraftRegionAccessorMixin {

    // @formatter:off
    @Shadow public abstract <T extends Entity> T addEntity(net.minecraft.world.entity.Entity entity, CreatureSpawnEvent.SpawnReason reason, Consumer<T> function, boolean randomizeData) throws IllegalArgumentException;
    // @formatter:on

    @Inject(method = "spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;)Lorg/bukkit/entity/Entity;", cancellable = true, at = @At("HEAD"))
    private void arclight$useFactory(Location loc, EntityType entityType, CallbackInfoReturnable<Entity> cir) {
        Function<Location, ? extends net.minecraft.world.entity.Entity> factory = ((EntityTypeBridge) (Object) entityType).bridge$entityFactory();
        if (factory != null) {
            cir.setReturnValue(this.addEntity(factory.apply(loc), CreatureSpawnEvent.SpawnReason.CUSTOM, null, true));
        }
    }

    @Inject(method = "spawnEntity(Lorg/bukkit/Location;Lorg/bukkit/entity/EntityType;Z)Lorg/bukkit/entity/Entity;", cancellable = true, at = @At("HEAD"))
    private void arclight$useFactory(Location loc, EntityType entityType, boolean randomizeData, CallbackInfoReturnable<Entity> cir) {
        Function<Location, ? extends net.minecraft.world.entity.Entity> factory = ((EntityTypeBridge) (Object) entityType).bridge$entityFactory();
        if (factory != null) {
            cir.setReturnValue(this.addEntity(factory.apply(loc), CreatureSpawnEvent.SpawnReason.CUSTOM, null, randomizeData));
        }
    }

    // Fusion Core: orbs spawned by plugins get Paper's CUSTOM reason (patch 0125)
    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "createEntity(Lorg/bukkit/Location;Ljava/lang/Class;Z)Lnet/minecraft/world/entity/Entity;")
    private net.minecraft.world.entity.Entity fusioncore$customOrbs(Location location, Class<? extends Entity> clazz, boolean randomizeData,
                                                                  com.llamalad7.mixinextras.injector.wrapoperation.Operation<net.minecraft.world.entity.Entity> original) {
        if (clazz != null && org.bukkit.entity.ExperienceOrb.class.isAssignableFrom(clazz)) {
            return io.izzel.arclight.common.mod.util.FusionCoreExperienceOrbs.with(org.bukkit.entity.ExperienceOrb$SpawnReason.CUSTOM, null, null,
                () -> original.call(location, clazz, randomizeData));
        }
        return original.call(location, clazz, randomizeData);
    }

    /** Paper ("Add FeatureFlag API"): {@code CraftWorld} keeps Spigot's override, limited regions answer from their level. */
    public java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        return io.izzel.arclight.common.mod.util.FusionCoreFeatureFlags.fromNms(((CraftRegionAccessor) (Object) this).getHandle().enabledFeatures());
    }
}
