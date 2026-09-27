package io.izzel.arclight.common.mixin.core.world.level.block.entity;

import io.izzel.arclight.common.bridge.core.world.item.crafting.RecipeHolderBridge;
import io.izzel.arclight.mixin.Decorate;
import io.izzel.arclight.mixin.DecorationOps;
import io.izzel.arclight.mixin.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v.block.CraftBlock;
import org.bukkit.craftbukkit.v.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.bukkit.event.block.CampfireStartEvent;
import org.bukkit.inventory.CampfireRecipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(CampfireBlockEntity.class)
public abstract class CampfireBlockEntityMixin extends BlockEntityMixin implements io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge {

    // @formatter:off
    @Shadow public abstract Optional<RecipeHolder<CampfireCookingRecipe>> getCookableRecipe(ItemStack p_59052_);
    @Shadow @Final public int[] cookingTime;
    // @formatter:on

    @Decorate(method = "cookTick", inject = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Containers;dropItemStack(Lnet/minecraft/world/level/Level;DDDLnet/minecraft/world/item/ItemStack;)V"))
    private static void arclight$cookEvent(Level level, BlockPos blockPos, BlockState blockState, CampfireBlockEntity campfireBlockEntity,
                                           @Local(ordinal = 0) ItemStack sourceStack, @Local(ordinal = -1) ItemStack resultStack) throws Throwable {
        CraftItemStack source = CraftItemStack.asCraftMirror(sourceStack);
        org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(resultStack);

        // Fusion Core: Paper's recipe on the event (patch 0497); the vanilla recipe lookup sits inside a lambda
        var recipe = level.getRecipeManager().getRecipeFor(net.minecraft.world.item.crafting.RecipeType.CAMPFIRE_COOKING,
            new net.minecraft.world.item.crafting.SingleRecipeInput(sourceStack), level).orElse(null);
        BlockCookEvent blockCookEvent = io.izzel.arclight.common.mod.util.FusionCoreEvents.newBlockCookEvent(CraftBlock.at(level, blockPos), source, result,
            io.izzel.arclight.common.mod.util.FusionCoreEvents.cookingRecipe(recipe));
        Bukkit.getPluginManager().callEvent(blockCookEvent);

        if (blockCookEvent.isCancelled()) {
            DecorationOps.cancel().invoke();
            return;
        }

        result = blockCookEvent.getResult();
        resultStack = CraftItemStack.asNMSCopy(result);
        DecorationOps.blackhole().invoke(resultStack);
    }

    @Inject(method = "placeFood", locals = LocalCapture.CAPTURE_FAILHARD,
        at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I"))
    private void arclight$cookStart(LivingEntity p_238285_, ItemStack stack, int p_238287_, CallbackInfoReturnable<Boolean> cir, int i) {
        var event = new CampfireStartEvent(CraftBlock.at(this.level, this.worldPosition), CraftItemStack.asCraftMirror(stack), (CampfireRecipe) ((RecipeHolderBridge) (Object) getCookableRecipe(stack).get()).bridge$toBukkitRecipe());
        Bukkit.getPluginManager().callEvent(event);
        this.cookingTime[i] = event.getTotalCookTime();
    }

    // Fusion Core: Paper patch 0634 "Add more Campfire API": a slot with cooking stopped keeps its item and progress.
    @org.spongepowered.asm.mixin.Unique private final boolean[] fusioncore$stopCooking = new boolean[4];

    @Override
    public boolean[] fusioncore$stopCooking() {
        return this.fusioncore$stopCooking;
    }

    @org.spongepowered.asm.mixin.injection.Redirect(method = "cookTick", at = @At(value = "FIELD", args = "array=set",
        target = "Lnet/minecraft/world/level/block/entity/CampfireBlockEntity;cookingProgress:[I"))
    private static void fusioncore$stopCooking(int[] progress, int slot, int value, Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (!((io.izzel.arclight.common.bridge.core.world.level.block.entity.BlockEntityPaperStateBridge) campfire).fusioncore$stopCooking()[slot]) {
            progress[slot] = value;
        }
    }

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    private void fusioncore$loadStopCooking(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (tag.contains("Paper.StopCooking", net.minecraft.nbt.Tag.TAG_BYTE_ARRAY)) {
            byte[] bytes = tag.getByteArray("Paper.StopCooking");
            for (int i = 0; i < Math.min(this.fusioncore$stopCooking.length, bytes.length); i++) {
                this.fusioncore$stopCooking[i] = bytes[i] == 1;
            }
        }
    }

    @Inject(method = "saveAdditional", at = @At("RETURN"))
    private void fusioncore$saveStopCooking(net.minecraft.nbt.CompoundTag tag, net.minecraft.core.HolderLookup.Provider provider, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        byte[] bytes = new byte[4];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (this.fusioncore$stopCooking[i] ? 1 : 0);
        }
        tag.putByteArray("Paper.StopCooking", bytes);
    }
}
