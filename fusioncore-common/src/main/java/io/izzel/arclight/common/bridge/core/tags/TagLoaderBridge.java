package io.izzel.arclight.common.bridge.core.tags;

import io.papermc.paper.tag.TagEventConfig;

/** Fusion Core: the tag events a registry's {@code TagLoader} fires (Paper's extra {@code build} parameter). */
public interface TagLoaderBridge {

    void fusioncore$setEventConfig(TagEventConfig<?, ?> config);
}
