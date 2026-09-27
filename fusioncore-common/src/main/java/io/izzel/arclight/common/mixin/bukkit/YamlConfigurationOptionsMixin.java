package io.izzel.arclight.common.mixin.bukkit;

import io.izzel.arclight.common.bridge.bukkit.YamlConfigurationOptionsBridge;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Fusion Core: Paper's code point limit (patch 0361): the longest document, in code points, that
 * {@code YamlConfiguration#loadFromString} reads. The default is Spigot's {@code Integer.MAX_VALUE}, as on Paper.
 */
@Mixin(value = YamlConfigurationOptions.class, remap = false)
public abstract class YamlConfigurationOptionsMixin implements YamlConfigurationOptionsBridge {

    private int codePointLimit = Integer.MAX_VALUE;

    /**
     * Gets the maximum code point limit, that being, the maximum length of the document
     * in which the loader will read
     *
     * @return The current value
     */
    public int codePointLimit() {
        return this.codePointLimit;
    }

    /**
     * Sets the maximum code point limit, that being, the maximum length of the document
     * in which the loader will read
     *
     * @param codePointLimit new codepoint limit
     * @return This object, for chaining
     */
    public YamlConfigurationOptions codePointLimit(int codePointLimit) {
        this.codePointLimit = codePointLimit;
        return (YamlConfigurationOptions) (Object) this;
    }

    @Override
    public int bridge$codePointLimit() {
        return this.codePointLimit;
    }
}
