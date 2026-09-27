package io.izzel.arclight.common.mod.util;

import com.google.common.base.Preconditions;
import io.izzel.arclight.common.mixin.core.world.entity.api.MobDropChanceApiInvoker;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.bukkit.craftbukkit.v.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

/** Fusion Core: bodies of Paper's inventory API that CraftBukkit keeps in its Craft classes. */
public final class FusionCoreInventories {

    /** Paper's {@code InventoryOpenEvent#titleOverride}; javac does not see the mixin's method. */
    public static net.kyori.adventure.text.Component titleOverride(org.bukkit.event.inventory.InventoryOpenEvent event) {
        return event == null ? null : ((io.izzel.arclight.common.bridge.bukkit.InventoryEventExtrasBridge.Open) event).bridge$titleOverride();
    }

    /** Paper's {@code FurnaceBurnEvent#willConsumeFuel}. */
    public static boolean willConsumeFuel(org.bukkit.event.inventory.FurnaceBurnEvent event) {
        return ((io.izzel.arclight.common.bridge.bukkit.InventoryEventExtrasBridge.FurnaceBurn) event).bridge$willConsumeFuel();
    }

    /** Writes {@code replacement} into {@code target} (type, count, components): Paper's {@code EnchantItemEvent#setItem}. */
    public static void replaceInPlace(net.minecraft.world.item.ItemStack target, org.bukkit.inventory.ItemStack replacement) {
        org.bukkit.inventory.ItemStack mirror = org.bukkit.craftbukkit.v.inventory.CraftItemStack.asCraftMirror(target);
        mirror.setType(replacement.getType());
        mirror.setAmount(replacement.getAmount());
        mirror.setItemMeta(replacement.getItemMeta());
    }

    private FusionCoreInventories() {
    }

    /** Paper patch 0238: Spigot's {@code CraftInventory#removeItem}, searching {@code getContents()} instead of the storage slots. */
    public static HashMap<Integer, ItemStack> removeItemAnySlot(Inventory inventory, ItemStack... items) {
        Preconditions.checkArgument(items != null, "items cannot be null");
        HashMap<Integer, ItemStack> leftover = new HashMap<>();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            Preconditions.checkArgument(item != null, "ItemStack cannot be null");
            int toDelete = item.getAmount();
            while (true) {
                int first = firstSimilar(item, inventory.getContents());
                if (first == -1) {
                    item.setAmount(toDelete);
                    leftover.put(i, item);
                    break;
                }
                ItemStack stack = inventory.getItem(first);
                int amount = stack.getAmount();
                if (amount <= toDelete) {
                    toDelete -= amount;
                    inventory.clear(first);
                } else {
                    stack.setAmount(amount - toDelete);
                    inventory.setItem(first, stack);
                    toDelete = 0;
                }
                if (toDelete <= 0) {
                    break;
                }
            }
        }
        return leftover;
    }

    private static int firstSimilar(ItemStack item, ItemStack[] contents) {
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null && item.isSimilar(contents[i])) {
                return i;
            }
        }
        return -1;
    }

    /** Paper patch 0642. */
    public static boolean isFuel(ItemStack stack) {
        return stack != null && !stack.getType().isAir() && AbstractFurnaceBlockEntity.isFuel(CraftItemStack.asNMSCopy(stack));
    }

    /** Paper patch 0642: whether the furnace's own recipe type (smelting, blasting, smoking) has a recipe for the item. */
    public static boolean canSmelt(Inventory furnaceInventory, ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        AbstractFurnaceBlockEntity furnace = (AbstractFurnaceBlockEntity) ((org.bukkit.craftbukkit.v.inventory.CraftInventory) furnaceInventory).getInventory();
        net.minecraft.server.level.ServerLevel world = ((org.bukkit.craftbukkit.v.CraftWorld) org.bukkit.Bukkit.getWorlds().get(0)).getHandle();
        // Paper asks the recipe manager for the furnace's recipe type (a NeoForge field); the furnace's own cached check is the same lookup
        return furnace.quickCheck.getRecipeFor(new SingleRecipeInput(CraftItemStack.asNMSCopy(stack)), world).isPresent();
    }

    /** Paper patch 0512: a player inventory always drops everything; non-mobs count as 1, as in Spigot. */
    public static float dropChance(EntityEquipment equipment, EquipmentSlot slot) {
        if (equipment instanceof PlayerInventory) {
            return 1;
        }
        if (equipment.getHolder() instanceof org.bukkit.craftbukkit.v.entity.CraftEntity entity && entity.getHandle() instanceof Mob mob) {
            return ((MobDropChanceApiInvoker) mob).fusioncore$getEquipmentDropChance(CraftEquipmentSlot.getNMS(slot));
        }
        return 1;
    }

    public static void setDropChance(EntityEquipment equipment, EquipmentSlot slot, float chance) {
        if (equipment instanceof PlayerInventory) {
            throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
        }
        Preconditions.checkArgument(equipment.getHolder() instanceof org.bukkit.craftbukkit.v.entity.CraftEntity entity && entity.getHandle() instanceof Mob,
            "Cannot set drop chance for non-Mob entity");
        ((Mob) ((org.bukkit.craftbukkit.v.entity.CraftEntity) equipment.getHolder()).getHandle()).setDropChance(CraftEquipmentSlot.getNMS(slot), chance);
    }
}
