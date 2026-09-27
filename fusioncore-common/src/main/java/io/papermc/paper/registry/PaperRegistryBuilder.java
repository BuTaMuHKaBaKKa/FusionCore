// Fusion Core: Paper 1.21.1 server class (patch 1014, registry modification), as in Paper.
package io.papermc.paper.registry;

import io.papermc.paper.registry.data.util.Conversions;
import net.minecraft.resources.RegistryOps;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface PaperRegistryBuilder<M, T> extends RegistryBuilder<T> {

    M build();

    @FunctionalInterface
    interface Filler<M, T, B extends PaperRegistryBuilder<M, T>> {

        B fill(Conversions conversions, TypedKey<T> key, @Nullable M nms);

        default Factory<M, T, B> asFactory() {
            return (lookup, key) -> this.fill(lookup, key, null);
        }
    }

    @FunctionalInterface
    interface Factory<M, T, B extends PaperRegistryBuilder<M, T>> {

        B create(Conversions conversions, TypedKey<T> key);
    }
}
