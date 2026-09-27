package io.izzel.arclight.common.mixin.core.server.level;

import io.izzel.arclight.common.bridge.core.server.level.ServerLevelPaperBridge;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import net.minecraft.world.level.storage.ServerLevelData;
import org.bukkit.event.weather.ThunderChangeEvent$Cause;
import org.bukkit.event.weather.WeatherChangeEvent$Cause;

import java.util.Optional;

/** Fusion Core: see {@link ServerLevelPaperBridge}. Void damage defaults are Paper's config defaults (= vanilla). */
@Mixin(ServerLevel.class)
public abstract class ServerLevelPaperMixin implements ServerLevelPaperBridge {

    // @formatter:off
    @Shadow private Optional<BlockPos> findLightningRod(BlockPos pos) { throw new AssertionError(); }
    // @formatter:on

    @Unique private boolean fusioncore$voidDamageEnabled = true;
    @Unique private float fusioncore$voidDamageAmount = 4.0F;
    @Unique private double fusioncore$voidDamageMinBuildHeightOffset = -64.0D;

    @Override
    public boolean fusioncore$voidDamageEnabled() {
        return this.fusioncore$voidDamageEnabled;
    }

    @Override
    public void fusioncore$setVoidDamageEnabled(boolean enabled) {
        this.fusioncore$voidDamageEnabled = enabled;
    }

    @Override
    public float fusioncore$voidDamageAmount() {
        return this.fusioncore$voidDamageAmount;
    }

    @Override
    public void fusioncore$setVoidDamageAmount(float amount) {
        this.fusioncore$voidDamageAmount = amount;
    }

    @Override
    public double fusioncore$voidDamageMinBuildHeightOffset() {
        return this.fusioncore$voidDamageMinBuildHeightOffset;
    }

    @Override
    public void fusioncore$setVoidDamageMinBuildHeightOffset(double offset) {
        this.fusioncore$voidDamageMinBuildHeightOffset = offset;
    }

    @Override
    public Optional<BlockPos> fusioncore$findLightningRod(BlockPos pos) {
        return this.findLightningRod(pos);
    }

    // ---- Paper's weather change causes (patch 0540): /weather, the natural cycle, and sleeping through the night

    @WrapOperation(method = "setWeatherParameters", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void fusioncore$commandRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        FusionCoreEvents.withRainCause(WeatherChangeEvent$Cause.COMMAND, () -> original.call(data, raining));
    }

    @WrapOperation(method = "setWeatherParameters", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void fusioncore$commandThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        FusionCoreEvents.withThunderCause(ThunderChangeEvent$Cause.COMMAND, () -> original.call(data, thundering));
    }

    @WrapOperation(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void fusioncore$naturalRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        FusionCoreEvents.withRainCause(WeatherChangeEvent$Cause.NATURAL, () -> original.call(data, raining));
    }

    @WrapOperation(method = "advanceWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void fusioncore$naturalThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        FusionCoreEvents.withThunderCause(ThunderChangeEvent$Cause.NATURAL, () -> original.call(data, thundering));
    }

    @WrapOperation(method = "resetWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setRaining(Z)V"))
    private void fusioncore$sleepRain(ServerLevelData data, boolean raining, Operation<Void> original) {
        FusionCoreEvents.withRainCause(WeatherChangeEvent$Cause.SLEEP, () -> original.call(data, raining));
    }

    @WrapOperation(method = "resetWeatherCycle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/ServerLevelData;setThundering(Z)V"))
    private void fusioncore$sleepThunder(ServerLevelData data, boolean thundering, Operation<Void> original) {
        FusionCoreEvents.withThunderCause(ThunderChangeEvent$Cause.SLEEP, () -> original.call(data, thundering));
    }
}
