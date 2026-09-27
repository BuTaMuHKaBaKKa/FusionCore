package io.izzel.arclight.common.mixin.bukkit.inventory;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.core.world.inventory.AbstractContainerMenuBridge;
import io.izzel.arclight.common.mod.util.FusionCoreAdventure;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.bukkit.craftbukkit.v.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v.inventory.CraftMenuType;
import org.bukkit.craftbukkit.v.inventory.util.CraftMenus;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

/**
 * Fusion Core: Paper's {@code CraftMenuType#create(HumanEntity, Component)} (Paper patch 0010 "Adventure"): Spigot's
 * {@code create(HumanEntity, String)} with the title kept as a component (no legacy round trip).
 */
@Mixin(value = CraftMenuType.class, remap = false)
public abstract class CraftMenuTypeMixin<V extends InventoryView> {

    @Shadow @Final private net.minecraft.world.inventory.MenuType<?> handle;
    @Shadow @Final private Supplier<CraftMenus.MenuTypeData<V>> typeData;

    @SuppressWarnings("unchecked")
    public V create(final HumanEntity player, final net.kyori.adventure.text.Component title) {
        Preconditions.checkArgument(player != null, "The given player must not be null");
        Preconditions.checkArgument(title != null, "The given title must not be null");
        Preconditions.checkArgument(player instanceof CraftHumanEntity, "The given player must be a CraftHumanEntity");
        final CraftHumanEntity craftHuman = (CraftHumanEntity) player;
        Preconditions.checkArgument(craftHuman.getHandle() instanceof ServerPlayer, "The given player must be an EntityPlayer");
        final ServerPlayer serverPlayer = (ServerPlayer) craftHuman.getHandle();

        final AbstractContainerMenu container = this.typeData.get().menuBuilder().build(serverPlayer, this.handle);
        ((AbstractContainerMenuBridge) container).bridge$setTitle(FusionCoreAdventure.asVanilla(title));
        ((AbstractContainerMenuBridge) container).fusioncore$setCheckReachable(false);
        return (V) ((AbstractContainerMenuBridge) container).bridge$getBukkitView();
    }
}
