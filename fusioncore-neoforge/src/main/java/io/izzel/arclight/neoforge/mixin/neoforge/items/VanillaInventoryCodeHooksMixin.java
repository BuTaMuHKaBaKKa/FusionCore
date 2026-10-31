package io.izzel.arclight.neoforge.mixin.neoforge.items;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.bridge.core.world.IInventoryBridge;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.mod.util.FusionCoreHoppers;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.neoforge.mod.util.DelegatedContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.VanillaInventoryCodeHooks;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftInventory;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(VanillaInventoryCodeHooks.class)
public abstract class VanillaInventoryCodeHooksMixin {

    @Inject(method = "getItemHandlerAt", at = @At("RETURN"), remap = false)
    private static void arclight$recordResult(Level worldIn, double x, double y, double z, Direction side, CallbackInfoReturnable<Optional<Pair<IItemHandler, Object>>> cir) {
        if (cir.getReturnValue().isPresent()) {
            DelegatedContainer.recordLastHandler();
        }
    }

    @Decorate(method = "insertHook", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/VanillaInventoryCodeHooks;getAttachedItemHandler(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Ljava/util/Optional;"))
    private static Optional<Pair<IItemHandler, Object>> arclight$searchTo(Level level, BlockPos pos, Direction direction, HopperBlockEntity hopperBlockEntity) throws Throwable {
        final var handler = (Optional<Pair<IItemHandler, Object>>) DecorationOps.callsite().invoke(level, pos, direction);
        if (HopperInventorySearchEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return handler; // Fusion Core: no listener - NeoForge's own result, no Bukkit round trip
        }
        final var hopper = CraftBlock.at(level, pos);
        final var searchBlock = CraftBlock.at(level, pos.relative(hopperBlockEntity.facing));
        final var container = handler.map(DelegatedContainer::new).orElse(null);
        return arclight$runHopperInventorySearchEvent(container, hopper, searchBlock, HopperInventorySearchEvent.ContainerType.DESTINATION);
    }

    @Decorate(method = {"lambda$dropperInsertHook$1", "lambda$insertHook$2", "lambda$insertCrafterOutput$3"}, at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/VanillaInventoryCodeHooks;putStackInInventoryAllSlots(Lnet/minecraft/world/level/block/entity/BlockEntity;Ljava/lang/Object;Lnet/neoforged/neoforge/items/IItemHandler;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack arclight$sourceInitiatedMoveItem(BlockEntity source, Object destination, IItemHandler instance, ItemStack stack) throws Throwable {
        // Fusion Core: no listener - the NeoForge transfer, without building the Bukkit inventories and event
        if (!stack.isEmpty() && InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length > 0) {
            CraftItemStack original = CraftItemStack.asCraftMirror(stack);

            Inventory destInventory = DelegatedContainer.getOwnerInventory(destination, instance);
            InventoryMoveItemEvent event = new InventoryMoveItemEvent(((IInventoryBridge) source).getOwnerInventory(), original.clone(), destInventory, true);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                if (source instanceof HopperBlockEntity hopper) {
                    hopper.setCooldown(((WorldBridge) source.getLevel()).bridge$spigotConfig().hopperTransfer);
                }
                // Delay hopper checks
                // Arclight: we can return stack directly so we use vanilla revert logic and eventually return false if none is transferred
                // Arclight: but CraftBukkit makes it delayed directly, don't know why, so have to catch the index to revert change?
                return stack;
            }
            stack = CraftItemStack.asNMSCopy(event.getItem());
        }
        return (ItemStack) DecorationOps.callsite().invoke(source, destination, instance, stack);
    }

    @Decorate(method = "extractHook", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/VanillaInventoryCodeHooks;getSourceItemHandler(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/entity/Hopper;)Ljava/util/Optional;"))
    private static Optional<Pair<IItemHandler, Object>> arclight$searchFrom(Level level, Hopper hopper) throws Throwable {
        final var handler = (Optional<Pair<IItemHandler, Object>>) DecorationOps.callsite().invoke(level, hopper);
        if (HopperInventorySearchEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return handler; // Fusion Core: no listener - NeoForge's own result, no Bukkit round trip
        }
        final var blockPos = BlockPos.containing(hopper.getLevelX(), hopper.getLevelY(), hopper.getLevelZ());
        final var hopperBlock = CraftBlock.at(level, blockPos);
        final var containerBlock = CraftBlock.at(level, blockPos.above());
        final var container = handler.map(DelegatedContainer::new).orElse(null);
        return arclight$runHopperInventorySearchEvent(container, hopperBlock, containerBlock, HopperInventorySearchEvent.ContainerType.SOURCE);
    }

    @Decorate(method = "lambda$extractHook$0", at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/neoforged/neoforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack arclight$nonSourceInitiatedMoveItem(IItemHandler instance, int slot, int expected, boolean simulate, Hopper hopper, Pair<IItemHandler, Object> sourcePair) throws Throwable {
        Preconditions.checkArgument(simulate, "Should be injected at simulate=true");
        // Fusion Core: Spigot's hopper-amount, the event carries all the items the hopper takes at once
        ItemStack stack = (ItemStack) DecorationOps.callsite().invoke(instance, slot, FusionCoreHoppers.amount(hopper), simulate);
        if (stack.isEmpty() || InventoryMoveItemEvent.getHandlerList().getRegisteredListeners().length == 0) {
            return stack;
        }
        Object destination = sourcePair.getRight();
        CraftItemStack original = CraftItemStack.asCraftMirror(stack);
        Inventory sourceInventory = DelegatedContainer.getOwnerInventory(destination, instance);

        InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, original.clone(), ((IInventoryBridge) hopper).getOwnerInventory(), false);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            if (hopper instanceof HopperBlockEntity entity) {
                entity.setCooldown(((WorldBridge) entity.getLevel()).bridge$spigotConfig().hopperTransfer);
            }
            // Delay hopper checks
            // Arclight: we can return stack directly so we use vanilla revert logic and eventually return false if none is transferred
            // Arclight: but CraftBukkit makes it delayed directly, don't know why, so have to catch the index to revert change?
            return ItemStack.EMPTY;
        }
        return CraftItemStack.asNMSCopy(event.getItem());
    }

    // Fusion Core: Spigot's hopper-amount when the hopper pulls: take as many as fit into the slot found
    @WrapOperation(method = "lambda$extractHook$0", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/Hopper;canPlaceItem(ILnet/minecraft/world/item/ItemStack;)Z")),
        at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/items/IItemHandler;extractItem(IIZ)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack fusioncore$extractAmount(IItemHandler handler, int slot, int count, boolean simulate, Operation<ItemStack> original,
                                                      @Local(argsOnly = true) Hopper hopper, @Local(ordinal = 0) ItemStack offered, @Local(ordinal = 1) ItemStack destStack) {
        int amount = FusionCoreHoppers.amount(hopper);
        if (amount > 1) {
            int room = destStack.isEmpty()
                ? Math.min(offered.getMaxStackSize(), hopper.getMaxStackSize())
                : Math.min(destStack.getMaxStackSize(), hopper.getMaxStackSize()) - destStack.getCount();
            count = Math.max(1, Math.min(amount, room));
        }
        return original.call(handler, slot, count, simulate);
    }

    @WrapOperation(method = "lambda$extractHook$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;grow(I)V"))
    private static void fusioncore$extractGrow(ItemStack destStack, int count, Operation<Void> original, @Local(ordinal = 0) ItemStack extracted) {
        original.call(destStack, Math.max(count, extracted.getCount()));
    }

    // Fusion Core: Spigot's hopper-amount when the hopper pushes; what did not fit goes back, what moved stays moved
    @WrapOperation(method = "lambda$insertHook$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;removeItem(II)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack fusioncore$insertAmount(HopperBlockEntity hopper, int slot, int count, Operation<ItemStack> original) {
        return original.call(hopper, slot, FusionCoreHoppers.amount(hopper));
    }

    @WrapOperation(method = "lambda$insertHook$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/HopperBlockEntity;setItem(ILnet/minecraft/world/item/ItemStack;)V"))
    private static void fusioncore$insertRest(HopperBlockEntity hopper, int slot, ItemStack originalSlotContents, Operation<Void> original,
                                              @Local(ordinal = 2) ItemStack remainder) {
        int amount = FusionCoreHoppers.amount(hopper);
        if (amount > 1) {
            int moved = Math.min(amount, originalSlotContents.getCount());
            originalSlotContents.shrink(moved - Math.min(moved, remainder.getCount()));
        }
        original.call(hopper, slot, originalSlotContents);
    }

    // Fusion Core: NeoForge puts a stack into a slot only when all of it fits, fine for one item at a time; with
    // hopper-amount above 1 the hopper fills the slot partly, as the vanilla transfer and Spigot do
    @Inject(method = "insertStack", cancellable = true, at = @At("HEAD"))
    private static void fusioncore$partialInsert(BlockEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot,
                                                 CallbackInfoReturnable<ItemStack> cir) {
        if (stack.getCount() <= 1 || !(source instanceof HopperBlockEntity hopper) || FusionCoreHoppers.amount(hopper) <= 1) {
            return;
        }
        ItemStack existing = destInventory.getStackInSlot(slot);
        if (!existing.isEmpty() && !ItemStack.isSameItemSameComponents(existing, stack)) {
            cir.setReturnValue(stack);
            return;
        }
        boolean wasEmpty = true;
        for (int i = 0; i < destInventory.getSlots(); i++) {
            if (destInventory.getStackInSlot(i).getCount() > 0) {
                wasEmpty = false;
                break;
            }
        }
        ItemStack rest = destInventory.insertItem(slot, stack, false);
        if (rest.getCount() < stack.getCount() && wasEmpty && destination instanceof HopperBlockEntity destinationHopper
            && !destinationHopper.isOnCustomCooldown()) {
            destinationHopper.setCooldown(8 - (destinationHopper.getLastUpdateTime() >= hopper.getLastUpdateTime() ? 1 : 0));
        }
        cir.setReturnValue(rest);
    }

    // Fusion Core: Paper's hopper.ignore-occluding-blocks, no search for container entities inside a full block
    @WrapOperation(method = "getItemHandlerAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;"))
    private static java.util.List<net.minecraft.world.entity.Entity> fusioncore$ignoreOccluding(Level level, net.minecraft.world.entity.Entity except, net.minecraft.world.phys.AABB box,
                                                                                                 java.util.function.Predicate<? super net.minecraft.world.entity.Entity> filter,
                                                                                                 Operation<java.util.List<net.minecraft.world.entity.Entity>> original,
                                                                                                 @Local net.minecraft.world.level.block.state.BlockState state) {
        return io.izzel.arclight.common.mod.util.FusionCoreWorldSettings.skipEntityContainers(level, state) ? java.util.List.of() : original.call(level, except, box, filter);
    }

    private static Optional<Pair<IItemHandler, Object>> arclight$runHopperInventorySearchEvent(Container inventory, CraftBlock hopper, CraftBlock searchLocation, HopperInventorySearchEvent.ContainerType containerType) {
        var event = new HopperInventorySearchEvent((inventory != null) ? new CraftInventory(inventory) : null, containerType, hopper, searchLocation);
        Bukkit.getServer().getPluginManager().callEvent(event);
        CraftInventory craftInventory = (CraftInventory) event.getInventory();
        return Optional.ofNullable(DelegatedContainer.makeItemHandlerPair(craftInventory));
    }

}
