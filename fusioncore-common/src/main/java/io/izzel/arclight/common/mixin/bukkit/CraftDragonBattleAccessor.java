package io.izzel.arclight.common.mixin.bukkit;

import net.minecraft.world.level.dimension.end.EndDragonFight;
import org.bukkit.craftbukkit.v.boss.CraftDragonBattle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = CraftDragonBattle.class, remap = false)
public interface CraftDragonBattleAccessor {

    @Accessor("handle")
    EndDragonFight fusioncore$getHandle();
}
