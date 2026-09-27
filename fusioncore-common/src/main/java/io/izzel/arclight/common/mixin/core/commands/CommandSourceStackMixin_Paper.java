package io.izzel.arclight.common.mixin.core.commands;

import io.izzel.arclight.common.bridge.core.commands.CommandSourceStackBridge;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/** Fusion Core: the command source implements Paper's {@link io.papermc.paper.command.brigadier.CommandSourceStack}. */
@Mixin(CommandSourceStack.class)
@SuppressWarnings("removal")
public abstract class CommandSourceStackMixin_Paper implements io.papermc.paper.command.brigadier.CommandSourceStack, com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource {

    // @formatter:off
    @Shadow public abstract ServerLevel getLevel();
    @Shadow public abstract Vec3 getPosition();
    @Shadow public abstract Vec2 getRotation();
    @Shadow public abstract Entity getEntity();
    // @formatter:on

    @Override
    public Location getLocation() {
        Vec2 rot = this.getRotation();
        Vec3 pos = this.getPosition();
        return new Location(((WorldBridge) this.getLevel()).bridge$getWorld(), pos.x, pos.y, pos.z, rot.y, rot.x);
    }

    @Override
    public CommandSender getSender() {
        return ((CommandSourceStackBridge) this).bridge$getBukkitSender();
    }

    @Override
    public org.bukkit.entity.Entity getExecutor() {
        Entity entity = this.getEntity();
        return entity == null ? null : ((EntityBridge) entity).bridge$getBukkitEntity();
    }

    // Fusion Core: Paper's old BukkitBrigadierCommandSource (patch 0276); getBukkitSender() is CraftBukkit's (CommandSourceStackMixin)

    @Override
    public org.bukkit.entity.Entity getBukkitEntity() {
        return this.getExecutor();
    }

    @Override
    public org.bukkit.World getBukkitWorld() {
        ServerLevel level = this.getLevel();
        return level == null ? null : ((WorldBridge) level).bridge$getWorld();
    }

    @Override
    public Location getBukkitLocation() {
        Vec3 pos = this.getPosition();
        org.bukkit.World world = this.getBukkitWorld();
        Vec2 rot = this.getRotation();
        return world != null && pos != null ? new Location(world, pos.x, pos.y, pos.z, rot != null ? rot.y : 0, rot != null ? rot.x : 0) : null;
    }
}
