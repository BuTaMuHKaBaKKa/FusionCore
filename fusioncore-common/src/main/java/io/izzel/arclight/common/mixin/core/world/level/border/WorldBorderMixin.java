package io.izzel.arclight.common.mixin.core.world.level.border;

import io.izzel.arclight.common.bridge.core.world.level.border.WorldBorderBridge;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(WorldBorder.class)
public class WorldBorderMixin implements WorldBorderBridge {

    @Shadow @Final private List<BorderChangeListener> listeners;
    public Level world;

    @Override
    public Level bridge$getWorld() {
        return this.world;
    }

    @Override
    public void bridge$setWorld(Level world) {
        this.world = world;
    }

    @Inject(method = "addListener", cancellable = true, at = @At("HEAD"))
    private void arclight$removeDuplicateListener(BorderChangeListener listener, CallbackInfo ci) {
        if (listeners.contains(listener)) {
            ci.cancel();
        }
    }

    // Fusion Core: Paper's world border events (Paper patch 0495) for the borders of worlds; borders without a world
    // (Bukkit#createWorldBorder, sent to single players) fire none, as in Paper

    @Shadow public double getCenterX() { return 0; }
    @Shadow public double getCenterZ() { return 0; }
    @Shadow public double getSize() { return 0; }
    @Shadow public void lerpSizeBetween(double fromSize, double toSize, long time) { }

    @org.spongepowered.asm.mixin.Unique
    private @org.jetbrains.annotations.Nullable org.bukkit.World fusioncore$bukkitWorld(org.bukkit.event.HandlerList handlers) {
        if (this.world == null || this.world.isClientSide || handlers.getRegisteredListeners().length == 0) {
            return null;
        }
        try {
            return ((io.izzel.arclight.common.bridge.core.world.level.WorldBridge) this.world).bridge$getWorld();
        } catch (UnsupportedOperationException e) {
            return null; // a level Bukkit has no world for (yet)
        }
    }

    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "setCenter")
    private void fusioncore$centerChangeEvent(double x, double z, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        org.bukkit.World world = this.fusioncore$bukkitWorld(io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent.getHandlerList());
        if (world != null) {
            io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent(world, world.getWorldBorder(),
                new org.bukkit.Location(world, this.getCenterX(), 0, this.getCenterZ()), new org.bukkit.Location(world, x, 0, z));
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            x = event.getNewCenter().getX();
            z = event.getNewCenter().getZ();
        }
        original.call(x, z);
    }

    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "setSize")
    private void fusioncore$sizeChangeEvent(double size, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        org.bukkit.World world = this.fusioncore$bukkitWorld(io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.getHandlerList());
        if (world != null) {
            io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent(world, world.getWorldBorder(),
                io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE, this.getSize(), size, 0);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            if (event.getType() == io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.STARTED_MOVE && event.getDuration() > 0) {
                this.lerpSizeBetween(event.getOldSize(), event.getNewSize(), event.getDuration());
                return;
            }
            size = event.getNewSize();
        }
        original.call(size);
    }

    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "lerpSizeBetween")
    private void fusioncore$lerpChangeEvent(double fromSize, double toSize, long time, com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        org.bukkit.World world = this.fusioncore$bukkitWorld(io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.getHandlerList());
        if (world != null) {
            io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent event = new io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent(world, world.getWorldBorder(),
                fromSize == toSize ? io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.INSTANT_MOVE : io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent.Type.STARTED_MOVE,
                fromSize, toSize, time);
            org.bukkit.Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            toSize = event.getNewSize();
            time = event.getDuration();
        }
        original.call(fromSize, toSize, time);
    }

    @Override
    public void bridge$borderMoveFinished(double from, double to, double duration) {
        org.bukkit.World world = this.fusioncore$bukkitWorld(io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent.getHandlerList());
        if (world != null) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent(world, world.getWorldBorder(), from, to, duration));
        }

    }
}
