package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.core.server.level.ServerLevelBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import org.bukkit.GameRule;
import org.bukkit.craftbukkit.v.CraftWorld;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.izzel.arclight.common.mod.util.FusionCoreEvents;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.event.weather.ThunderChangeEvent$Cause;
import org.bukkit.event.weather.WeatherChangeEvent$Cause;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;

@Mixin(CraftWorld.class)
public abstract class CraftWorldMixin {

    // @formatter:off
    @Shadow @Final private ServerLevel world;
    @Shadow(remap = false) public abstract boolean isGameRule(String rule);
    // @formatter:on

    // Fusion Core: the vanilla nether and end read their difficulty from the server's world data (DerivedLevelData ignores writes), so
    // setDifficulty on them did nothing; it sets the server's difficulty, as /difficulty does, for every world sharing it. Worlds
    // with their own data (created through WorldCreator) keep their own difficulty
    @org.spongepowered.asm.mixin.injection.Inject(method = "setDifficulty", remap = false, cancellable = true, at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void fusioncore$sharedDifficulty(org.bukkit.Difficulty difficulty, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (this.world.getLevelData() instanceof net.minecraft.world.level.storage.DerivedLevelData) {
            this.world.getServer().setDifficulty(net.minecraft.world.Difficulty.byId(difficulty.getValue()), true);
            ci.cancel();
        }
    }

    /**
     * @author Fusion Core
     * @reason return the canonical flag objects, as Paper does
     */
    @Overwrite(remap = false)
    public java.util.Set<org.bukkit.FeatureFlag> getFeatureFlags() {
        return io.izzel.arclight.common.mod.util.FusionCoreFeatureFlags.fromNms(((CraftWorld) (Object) this).getHandle().enabledFeatures());
    }

    /**
     * @author IzzelAliz
     * @reason
     */
    @Overwrite(remap = false)
    public File getWorldFolder() {
        return ((ServerLevelBridge) this.world).bridge$getConvertable().getDimensionPath(this.world.dimension()).toFile();
    }

    @Inject(method = "convert", cancellable = true, at = @At("HEAD"), remap = false)
    private<T> void arclight$fallbackConvert(GameRule<T> rule, GameRules.Value<?> value, CallbackInfoReturnable<T> cir) {
        if (rule.getType() == String.class) {
            cir.setReturnValue(rule.getType().cast(value.serialize()));
        }
    }

    @Redirect(method = "getGameRuleValue(Ljava/lang/String;)Ljava/lang/String;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules$Value;toString()Ljava/lang/String;"))
    private String arclight$useSerialize(GameRules.Value<?> instance) {
        return instance.serialize();
    }

    // ---- Fusion Core: Paper's weather cause PLUGIN (patch 0540)

    @WrapMethod(method = "setStorm", remap = false)
    private void fusioncore$pluginRain(boolean hasStorm, Operation<Void> original) {
        FusionCoreEvents.withRainCause(WeatherChangeEvent$Cause.PLUGIN, () -> original.call(hasStorm));
    }

    @WrapMethod(method = "setThundering", remap = false)
    private void fusioncore$pluginThunder(boolean thundering, Operation<Void> original) {
        FusionCoreEvents.withThunderCause(ThunderChangeEvent$Cause.PLUGIN, () -> original.call(thundering));
    }

    // ---- Fusion Core: Paper's WorldGameRuleChangeEvent for plugin changes, without a command sender (patch 0460)

    @WrapMethod(method = "setGameRuleValue", remap = false)
    private boolean fusioncore$gameRuleEvent(String rule, String value, Operation<Boolean> original) {
        if (rule == null || value == null || !this.isGameRule(rule)) {
            return original.call(rule, value);
        }
        WorldGameRuleChangeEvent event = new WorldGameRuleChangeEvent((org.bukkit.World) (Object) this, null, GameRule.getByName(rule), value);
        if (!io.izzel.arclight.common.mod.util.FusionCoreEvents.call(event)) {
            return false;
        }
        return original.call(rule, event.getValue());
    }

    @WrapMethod(method = "setGameRule", remap = false)
    private <T> boolean fusioncore$gameRuleEvent(GameRule<T> rule, T newValue, Operation<Boolean> original) {
        if (rule == null || newValue == null || !this.isGameRule(rule.getName())) {
            return original.call(rule, newValue);
        }
        WorldGameRuleChangeEvent event = new WorldGameRuleChangeEvent((org.bukkit.World) (Object) this, null, rule, String.valueOf(newValue));
        if (!io.izzel.arclight.common.mod.util.FusionCoreEvents.call(event)) {
            return false;
        }
        String value = event.getValue();
        if (value.equals(String.valueOf(newValue))) {
            return original.call(rule, newValue);
        }
        // the listener changed the value: CraftWorld stores newValue.toString(), so hand it a value that prints as the new one
        Object changed;
        try {
            changed = rule.getType() == Boolean.class ? Boolean.valueOf(value)
                : rule.getType() == Integer.class ? (Object) Integer.valueOf(value.trim()) : value;
        } catch (NumberFormatException e) {
            changed = newValue; // not a number: keep the value the plugin asked for
        }
        return original.call(rule, rule.getType().cast(changed));
    }
}
