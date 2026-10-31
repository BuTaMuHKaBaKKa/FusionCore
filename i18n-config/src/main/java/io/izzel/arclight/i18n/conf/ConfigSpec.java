package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class ConfigSpec {

    @Setting("_v")
    private int version;

    @Setting("optimization")
    private OptimizationSpec optimizationSpec;

    @Setting("locale")
    private LocaleSpec localeSpec;

    @Setting("compatibility")
    private CompatSpec compatSpec;

    @Setting("async-catcher")
    private AsyncCatcherSpec asyncCatcherSpec;

    @Setting("velocity")
    private VelocitySpec velocitySpec;

    @Setting("experimental")
    private ExperimentalSpec experimentalSpec;

    @Setting("lootables")
    private LootablesSpec lootablesSpec;

    @Setting("machines")
    private MachinesSpec machinesSpec;

    @Setting("world")
    private WorldSpec worldSpec;

    @Setting("anti-xray")
    private AntiXraySpec antiXraySpec;

    @Setting("spam-limiter")
    private SpamLimiterSpec spamLimiterSpec;

    @Setting("tpsbar")
    private TpsBarSpec tpsBarSpec;

    public int getVersion() {
        return version;
    }

    public OptimizationSpec getOptimization() {
        return optimizationSpec;
    }

    public LocaleSpec getLocale() {
        return localeSpec;
    }

    public CompatSpec getCompat() {
        return compatSpec;
    }

    public AsyncCatcherSpec getAsyncCatcher() {
        return asyncCatcherSpec;
    }

    public VelocitySpec getVelocity() {
        return velocitySpec;
    }

    public ExperimentalSpec getExperimental() {
        return experimentalSpec;
    }

    public LootablesSpec getLootables() {
        return lootablesSpec;
    }

    public MachinesSpec getMachines() {
        return machinesSpec;
    }

    public AntiXraySpec getAntiXray() {
        return antiXraySpec == null ? new AntiXraySpec() : antiXraySpec;
    }

    public SpamLimiterSpec getSpamLimiter() {
        return spamLimiterSpec == null ? new SpamLimiterSpec() : spamLimiterSpec;
    }

    public TpsBarSpec getTpsBar() {
        return tpsBarSpec == null ? new TpsBarSpec() : tpsBarSpec;
    }

    public WorldSpec getWorld() {
        return worldSpec == null ? new WorldSpec() : worldSpec;
    }
}
