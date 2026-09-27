package io.izzel.arclight.neoforge.mod.event;

import io.izzel.arclight.common.bridge.core.server.level.ServerPlayerBridge;
import io.izzel.arclight.common.mod.util.ArclightCaptures;
import io.izzel.arclight.common.mod.util.DistValidate;
import io.izzel.arclight.common.mod.util.FusionCoreBlockExperience;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakEventDispatcher {

    public BlockBreakEventDispatcher() {
        // Fusion Core: block experience as NeoForge's BlockDropsEvent computes it
        FusionCoreBlockExperience.setSource((state, level, pos, blockEntity, breaker, tool) ->
            EnchantmentHelper.processBlockExperience(level, tool, state.getExpDrop(level, pos, blockEntity, breaker, tool)));
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onBreakBlock(BlockEvent.BreakEvent event) {
        if (DistValidate.isValid(event.getLevel())) {
            CraftBlock craftBlock = CraftBlock.at(event.getLevel(), event.getPos());
            BlockBreakEvent breakEvent = new BlockBreakEvent(craftBlock, ((ServerPlayerBridge) event.getPlayer()).bridge$getBukkitEntity());
            ArclightCaptures.captureBlockBreakPlayer(breakEvent);
            breakEvent.setCancelled(event.isCanceled());
            // Fusion Core: Spigot's default experience (survival, a tool that harvests the block), given to NeoForge's
            // BlockDropsEvent below or dropped at the end of the break
            ServerLevel level = (ServerLevel) event.getLevel();
            if (!event.isCanceled() && !event.getPlayer().isCreative() && event.getState().canHarvestBlock(level, event.getPos(), event.getPlayer())) {
                ItemStack tool = event.getPlayer().getMainHandItem();
                breakEvent.setExpToDrop(FusionCoreBlockExperience.defaultExp(event.getState(), level, event.getPos(), level.getBlockEntity(event.getPos()), event.getPlayer(), tool));
            }
            Bukkit.getPluginManager().callEvent(breakEvent);
            event.setCanceled(breakEvent.isCancelled());
            if (!breakEvent.isCancelled()) {
                FusionCoreBlockExperience.expect(level, event.getPos(), breakEvent.getExpToDrop());
            }
        }
    }

    // Fusion Core: the experience a Spigot BlockBreakEvent / Paper BlockDestroyEvent decided, set first so mods can still adjust it
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onBlockDrops(BlockDropsEvent event) {
        Integer experience = FusionCoreBlockExperience.take(event.getLevel(), event.getPos());
        if (experience != null) {
            event.setDroppedExperience(experience);
        }
    }
}
