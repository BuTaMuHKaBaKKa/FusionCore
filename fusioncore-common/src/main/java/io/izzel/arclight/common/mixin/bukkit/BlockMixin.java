package io.izzel.arclight.common.mixin.bukkit;

import org.spongepowered.asm.mixin.Mixin;

/** Fusion Core: Paper's Block additions (breakNaturally with effect and experience, sound group, random tick, getState(boolean)). */
@Mixin(value = org.bukkit.block.Block.class, remap = false)
public interface BlockMixin {

    /** Paper: Spigot has no live block states, so both kinds are snapshots here. */
    default org.bukkit.block.BlockState getState(boolean useSnapshot) {
        return ((org.bukkit.block.Block) this).getState();
    }

    default boolean breakNaturally(boolean triggerEffect, boolean dropExperience) {
        return this.breakNaturally(null, triggerEffect, dropExperience);
    }

    default boolean breakNaturally(org.bukkit.inventory.ItemStack tool, boolean triggerEffect, boolean dropExperience) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlocks.breakNaturally((org.bukkit.block.Block) this, tool, triggerEffect, dropExperience);
    }

    /** Paper (patch 0289, deprecated there): the sounds of the block type's default state, as in Paper. */
    @Deprecated
    @SuppressWarnings("removal")
    default com.destroystokyo.paper.block.BlockSoundGroup getSoundGroup() {
        return new com.destroystokyo.paper.block.CraftBlockSoundGroup(
            ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().getBlock().defaultBlockState().getSoundType());
    }

    default org.bukkit.SoundGroup getBlockSoundGroup() {
        return ((org.bukkit.block.Block) this).getBlockData().getSoundGroup();
    }

    default void randomTick() {
        io.izzel.arclight.common.mod.util.FusionCoreBlocks.randomTick((org.bukkit.block.Block) this);
    }

    // Fusion Core: Paper's block API (Paper patch numbers at each group)

    // API patch "Add Block#getBlockKey": the packed position (statics in FusionCoreBlockStatics)

    default long getBlockKey() {
        org.bukkit.block.Block self = (org.bukkit.block.Block) this;
        return io.izzel.arclight.common.mod.util.FusionCoreBlockStatics.getBlockKey(self.getX(), self.getY(), self.getZ());
    }

    default boolean breakNaturally(boolean triggerEffect) {
        return this.breakNaturally(triggerEffect, false);
    }

    default boolean breakNaturally(org.bukkit.inventory.ItemStack tool, boolean triggerEffect) {
        return this.breakNaturally(tool, triggerEffect, false);
    }

    // patch 0730 "Block Ticking API"

    default void tick() {
        org.bukkit.craftbukkit.v.block.CraftBlock self = (org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this;
        net.minecraft.server.level.ServerLevel level = ((org.bukkit.craftbukkit.v.CraftWorld) self.getWorld()).getHandle();
        self.getNMS().tick(level, self.getPosition(), level.random);
    }

    default void fluidTick() {
        org.bukkit.craftbukkit.v.block.CraftBlock self = (org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this;
        self.getHandle().getFluidState(self.getPosition()).tick(((org.bukkit.craftbukkit.v.CraftWorld) self.getWorld()).getHandle(), self.getPosition());
    }

    // patch 0450 "Additional Block Material API" and patch 0590 "Add hasCollision methods to various places"

    default boolean isBuildable() {
        return ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().isSolid(); // Paper: "in fact isSolid"
    }

    default boolean isBurnable() {
        return ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().ignitedByLava();
    }

    default boolean isReplaceable() {
        return ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().canBeReplaced();
    }

    default boolean isSolid() {
        return ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().blocksMotion();
    }

    default boolean isCollidable() {
        return ((io.izzel.arclight.common.mixin.core.world.level.block.state.BlockBehaviourPaperAccessor)
            ((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS().getBlock()).fusioncore$hasCollision();
    }

    // patch 0498 "Add Block#isValidTool"

    default boolean isValidTool(org.bukkit.inventory.ItemStack itemStack) {
        return !((org.bukkit.block.Block) this).getDrops(itemStack).isEmpty();
    }

    // patch 0677 "Implement getComputedBiome API"

    default org.bukkit.block.Biome getComputedBiome() {
        org.bukkit.block.Block self = (org.bukkit.block.Block) this;
        return io.izzel.arclight.common.mod.util.FusionCoreWorlds.computedBiome(self.getWorld(), self.getX(), self.getY(), self.getZ());
    }

    // patch 0432 "Add Destroy Speed API"

    default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlocks.destroySpeed(((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS(), itemStack, false);
    }

    default float getDestroySpeed(org.bukkit.inventory.ItemStack itemStack, boolean considerEnchants) {
        return io.izzel.arclight.common.mod.util.FusionCoreBlocks.destroySpeed(((org.bukkit.craftbukkit.v.block.CraftBlock) (Object) this).getNMS(), itemStack, considerEnchants);
    }
}
