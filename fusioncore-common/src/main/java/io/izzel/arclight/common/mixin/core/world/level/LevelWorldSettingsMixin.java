package io.izzel.arclight.common.mixin.core.world.level;

import io.izzel.arclight.common.mod.util.FusionCoreWorldSettings;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

// Fusion Core: each level keeps its section of fusioncore.conf world (with world.worlds.<folder> over it), resolved on first use
// once the level has its Bukkit world, and the per-level state of Anti-Xray and optimize-mob-despawn (FusionCoreLevelData)
@Mixin(Level.class)
public abstract class LevelWorldSettingsMixin implements FusionCoreWorldSettings.Holder, io.izzel.arclight.common.mod.util.FusionCoreLevelData {

    private volatile FusionCoreWorldSettings.Settings fusioncore$settings;
    private volatile Object fusioncore$antiXray;
    private Object fusioncore$despawnIndex;

    @Override
    public Object fusioncore$antiXray() {
        return this.fusioncore$antiXray;
    }

    @Override
    public void fusioncore$setAntiXray(Object controller) {
        this.fusioncore$antiXray = controller;
    }

    @Override
    public Object fusioncore$despawnIndex() {
        return this.fusioncore$despawnIndex;
    }

    @Override
    public void fusioncore$setDespawnIndex(Object index) {
        this.fusioncore$despawnIndex = index;
    }

    @Override
    public FusionCoreWorldSettings.Settings fusioncore$worldSettings() {
        FusionCoreWorldSettings.Settings settings = this.fusioncore$settings;
        if (settings == null) {
            settings = FusionCoreWorldSettings.resolve((Level) (Object) this);
            if (settings == null) {
                return FusionCoreWorldSettings.common();
            }
            this.fusioncore$settings = settings;
        }
        return settings;
    }
}
