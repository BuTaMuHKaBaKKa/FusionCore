package io.izzel.arclight.common.mixin.optimization.general.activationrange;

import com.llamalad7.mixinextras.sugar.Local;
import io.izzel.arclight.common.bridge.core.entity.EntityBridge;
import io.izzel.arclight.common.bridge.core.world.level.WorldBridge;
import io.izzel.arclight.common.bridge.optimization.EntityBridge_ActivationRange;
import io.izzel.arclight.common.mod.ArclightConstants;
import io.izzel.arclight.common.mod.util.DistValidate;
import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spigotmc.ActivationRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin_ActivationRange implements EntityBridge_ActivationRange, EntityBridge, FusionCoreActivation.EntityState {

    // @formatter:off
    @Shadow public abstract void refreshDimensions();
    @Shadow public int tickCount;
    @Shadow public abstract Level level();
    @Shadow public abstract AABB getBoundingBox();
    @Shadow public abstract void discard();
    @Shadow public abstract Vec3 getDeltaMovement();
    @Shadow public abstract void setDeltaMovement(Vec3 vec);
    // @formatter:on

    // Spigot's type, still read by the tracking ranges
    public ActivationRange.ActivationType activationType;
    public boolean defaultActivationState;
    public long activatedTick = Integer.MIN_VALUE;
    // Fusion Core: Paper's Entity Activation Range 2.0
    @Unique private FusionCoreActivation.Type fusioncore$activationType = FusionCoreActivation.Type.MISC;
    @Unique private long fusioncore$activatedImmunityTick = Integer.MIN_VALUE;
    @Unique private boolean fusioncore$temporarilyActive;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void arclight$init(EntityType<?> entityTypeIn, Level worldIn, CallbackInfo ci) {
        activationType = ActivationRange.initializeEntityActivationType((Entity) (Object) this);
        this.fusioncore$activationType = FusionCoreActivation.typeOf((Entity) (Object) this);
        if (DistValidate.isValid(worldIn)) {
            var config = ((WorldBridge) worldIn).bridge$spigotConfig();
            if (config != null) {
                this.defaultActivationState = FusionCoreActivation.defaultActive((Entity) (Object) this, this.fusioncore$activationType, config);
            } else {
                this.defaultActivationState = false;
            }
        } else {
            this.defaultActivationState = false;
        }
    }

    public void inactiveTick() {
    }

    @Override
    public void bridge$inactiveTick() {
        this.inactiveTick();
    }

    @Override
    public void bridge$updateActivation() {
        if (ArclightConstants.currentTick > this.activatedTick && this.defaultActivationState) {
            this.activatedTick = ArclightConstants.currentTick;
        }
    }

    // Fusion Core: Paper, an entity moved by a piston stays active for a second
    @Inject(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;limitPistonMovement(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    private void fusioncore$pistonImmunity(MoverType type, Vec3 movement, CallbackInfo ci) {
        FusionCoreActivation.immunize((Entity) (Object) this, 20);
    }

    // Fusion Core: Paper, an entity ticked only to check its immunities does not move by itself
    @Inject(method = "move", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;maybeBackOffFromEdge(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/entity/MoverType;)Lnet/minecraft/world/phys/Vec3;"))
    private void fusioncore$ignoreInactiveMove(MoverType type, Vec3 original, CallbackInfo ci, @Local(argsOnly = true) Vec3 movement) {
        if (this.fusioncore$temporarilyActive && !((Object) this instanceof ItemEntity) && movement == this.getDeltaMovement() && type == MoverType.SELF) {
            this.setDeltaMovement(Vec3.ZERO);
            this.level().getProfiler().pop();
            ci.cancel();
        }
    }

    @Override
    public FusionCoreActivation.Type fusioncore$activationType() {
        return this.fusioncore$activationType;
    }

    @Override
    public long fusioncore$getActivatedTick() {
        return this.activatedTick;
    }

    @Override
    public void fusioncore$setActivatedTick(long tick) {
        this.activatedTick = tick;
    }

    @Override
    public long fusioncore$getImmunityTick() {
        return this.fusioncore$activatedImmunityTick;
    }

    @Override
    public void fusioncore$setImmunityTick(long tick) {
        this.fusioncore$activatedImmunityTick = tick;
    }

    @Override
    public boolean fusioncore$isTemporarilyActive() {
        return this.fusioncore$temporarilyActive;
    }

    @Override
    public void fusioncore$setTemporarilyActive(boolean active) {
        this.fusioncore$temporarilyActive = active;
    }

    @Override
    public boolean fusioncore$defaultActivationState() {
        return this.defaultActivationState;
    }
}
