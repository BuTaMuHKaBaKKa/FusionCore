package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.world.entity.monster.PhantomBridge;
import net.minecraft.world.entity.monster.Phantom;
import org.bukkit.craftbukkit.v.entity.CraftPhantom;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

/**
 * Fusion Core: Paper's {@code Phantom#getSpawningEntity()} (see {@link PhantomMixin}): the UUID of
 * the player the phantom was spawned for, or {@code null}.
 */
@Mixin(value = CraftPhantom.class, remap = false)
public abstract class CraftPhantomMixin {

    @Shadow public abstract Phantom getHandle();

    public UUID getSpawningEntity() {
        return ((PhantomBridge) this.getHandle()).bridge$getSpawningEntity();
    }
}
