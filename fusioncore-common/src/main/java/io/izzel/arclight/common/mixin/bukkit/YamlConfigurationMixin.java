package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.YamlConfigurationOptionsBridge;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yaml.snakeyaml.LoaderOptions;

/** Fusion Core: Paper's code point limit (patch 0361) applied to the loader before each document is read. */
@Mixin(value = YamlConfiguration.class, remap = false)
public abstract class YamlConfigurationMixin {

    @Shadow @Final private LoaderOptions yamlLoaderOptions;

    @Shadow public abstract YamlConfigurationOptions options();

    @Inject(method = "loadFromString", at = @At("HEAD"))
    private void fusioncore$codePointLimit(String contents, CallbackInfo ci) {
        this.yamlLoaderOptions.setCodePointLimit(((YamlConfigurationOptionsBridge) (Object) this.options()).bridge$codePointLimit());
    }
}
