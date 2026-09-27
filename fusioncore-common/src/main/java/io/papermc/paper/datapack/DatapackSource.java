// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.datapack;


/**
 * Source of a datapack.
 */
public sealed interface DatapackSource permits DatapackSourceImpl {

    DatapackSource DEFAULT = create("default");
    DatapackSource BUILT_IN = create("built_in");
    DatapackSource FEATURE = create("feature");
    DatapackSource WORLD = create("world");
    DatapackSource SERVER = create("server");

    private static DatapackSource create(final String name) {
        return new DatapackSourceImpl(name);
    }
}
