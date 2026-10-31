package io.izzel.arclight.common.mixin.optimization.general.activationrange;

import io.izzel.arclight.common.mod.util.FusionCoreActivation;
import org.spigotmc.SpigotWorldConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fusion Core: Paper's spigot.yml keys of Entity Activation Range 2.0 (water, villagers, flying monsters, wake-up-inactive,
// villager immunities), read with the rest of the world's settings and written to spigot.yml with Paper's defaults
@Mixin(value = SpigotWorldConfig.class, remap = false)
public abstract class SpigotWorldConfigMixin_ActivationRange implements FusionCoreActivation.ConfigState {

    // @formatter:off
    @Shadow private int getInt(String path, int def) { return 0; }
    @Shadow private boolean getBoolean(String path, boolean def) { return false; }
    // @formatter:on

    @Unique private final FusionCoreActivation.Settings fusioncore$activation = new FusionCoreActivation.Settings();

    @Inject(method = "init", at = @At("RETURN"))
    private void fusioncore$readActivation(CallbackInfo ci) {
        FusionCoreActivation.Settings s = this.fusioncore$activation;
        s.waterActivationRange = this.getInt("entity-activation-range.water", s.waterActivationRange);
        s.villagerActivationRange = this.getInt("entity-activation-range.villagers", s.villagerActivationRange);
        s.flyingMonsterActivationRange = this.getInt("entity-activation-range.flying-monsters", s.flyingMonsterActivationRange);

        s.wakeUpInactiveAnimals = this.getInt("entity-activation-range.wake-up-inactive.animals-max-per-tick", s.wakeUpInactiveAnimals);
        s.wakeUpInactiveAnimalsEvery = this.getInt("entity-activation-range.wake-up-inactive.animals-every", s.wakeUpInactiveAnimalsEvery);
        s.wakeUpInactiveAnimalsFor = this.getInt("entity-activation-range.wake-up-inactive.animals-for", s.wakeUpInactiveAnimalsFor);

        s.wakeUpInactiveMonsters = this.getInt("entity-activation-range.wake-up-inactive.monsters-max-per-tick", s.wakeUpInactiveMonsters);
        s.wakeUpInactiveMonstersEvery = this.getInt("entity-activation-range.wake-up-inactive.monsters-every", s.wakeUpInactiveMonstersEvery);
        s.wakeUpInactiveMonstersFor = this.getInt("entity-activation-range.wake-up-inactive.monsters-for", s.wakeUpInactiveMonstersFor);

        s.wakeUpInactiveVillagers = this.getInt("entity-activation-range.wake-up-inactive.villagers-max-per-tick", s.wakeUpInactiveVillagers);
        s.wakeUpInactiveVillagersEvery = this.getInt("entity-activation-range.wake-up-inactive.villagers-every", s.wakeUpInactiveVillagersEvery);
        s.wakeUpInactiveVillagersFor = this.getInt("entity-activation-range.wake-up-inactive.villagers-for", s.wakeUpInactiveVillagersFor);

        s.wakeUpInactiveFlying = this.getInt("entity-activation-range.wake-up-inactive.flying-monsters-max-per-tick", s.wakeUpInactiveFlying);
        s.wakeUpInactiveFlyingEvery = this.getInt("entity-activation-range.wake-up-inactive.flying-monsters-every", s.wakeUpInactiveFlyingEvery);
        s.wakeUpInactiveFlyingFor = this.getInt("entity-activation-range.wake-up-inactive.flying-monsters-for", s.wakeUpInactiveFlyingFor);

        s.villagersWorkImmunityAfter = this.getInt("entity-activation-range.villagers-work-immunity-after", s.villagersWorkImmunityAfter);
        s.villagersWorkImmunityFor = this.getInt("entity-activation-range.villagers-work-immunity-for", s.villagersWorkImmunityFor);
        s.villagersActiveForPanic = this.getBoolean("entity-activation-range.villagers-active-for-panic", s.villagersActiveForPanic);
    }

    @Override
    public FusionCoreActivation.Settings fusioncore$activation() {
        return this.fusioncore$activation;
    }
}
