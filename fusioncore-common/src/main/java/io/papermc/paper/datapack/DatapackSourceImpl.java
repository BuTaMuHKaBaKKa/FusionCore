// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.datapack;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
record DatapackSourceImpl(String name) implements DatapackSource {

    @Override
    public String toString() {
        return this.name;
    }
}
