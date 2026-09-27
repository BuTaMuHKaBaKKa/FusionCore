package io.izzel.arclight.common.mod;

import io.izzel.arclight.api.ArclightPlatform;
import io.izzel.arclight.common.mod.util.log.ArclightI18nLogger;
import io.izzel.arclight.mixin.MixinTools;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class ArclightConnector implements IMixinConnector {

    public static final Logger LOGGER = ArclightI18nLogger.getLogger("Fusion Core");

    @Override
    public void connect() {
        MixinTools.setup();
        Mixins.addConfiguration("mixins.fusioncore.core.json");
        Mixins.addConfiguration("mixins.fusioncore.bukkit.json");
        switch (ArclightPlatform.current()) {
            case VANILLA -> Mixins.addConfiguration("mixins.fusioncore.vanilla.json");
            case FORGE -> Mixins.addConfiguration("mixins.fusioncore.forge.json");
            case NEOFORGE -> Mixins.addConfiguration("mixins.fusioncore.neoforge.json");
        }
        LOGGER.info("mixin-load.core");
        Mixins.addConfiguration("mixins.fusioncore.impl.optimization.json");
        LOGGER.info("mixin-load.optimization");
    }
}
