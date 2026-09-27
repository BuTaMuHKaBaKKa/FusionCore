package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import org.bukkit.craftbukkit.v.block.CraftBeacon;
import org.bukkit.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.ArrayList;
import java.util.Collection;

/** Fusion Core: collects players in range here, since NeoForge's beacon lacks {@code getHumansInRange}. */
@Mixin(value = CraftBeacon.class, remap = false)
public abstract class CraftBeaconMixin {

    /**
     * @author Fusion Core
     * @reason getHumansInRange does not exist on NeoForge
     */
    @Overwrite
    public Collection<LivingEntity> getEntitiesInRange() {
        CraftBeacon self = (CraftBeacon) (Object) this;
        Collection<LivingEntity> bukkit = new ArrayList<>();
        if (!self.isPlaced()) {
            return bukkit;
        }
        net.minecraft.world.level.Level level = ((org.bukkit.craftbukkit.v.CraftWorld) self.getWorld()).getHandle();
        net.minecraft.core.BlockPos pos = new net.minecraft.core.BlockPos(self.getX(), self.getY(), self.getZ());
        if (level.getBlockEntity(pos) instanceof BeaconBlockEntity beacon) {
            for (net.minecraft.world.entity.player.Player human : io.izzel.arclight.common.mod.util.FusionCoreBlocks.beaconPlayers(beacon)) {
                bukkit.add((LivingEntity) ((io.izzel.arclight.common.bridge.core.entity.EntityBridge) human).bridge$getBukkitEntity());
            }
        }
        return bukkit;
    }
}
