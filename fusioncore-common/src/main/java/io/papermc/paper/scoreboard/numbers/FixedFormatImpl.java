// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.scoreboard.numbers;

import net.kyori.adventure.text.Component;

record FixedFormatImpl(Component component) implements FixedFormat {

    @Override
    public Component asComponent() {
        return this.component();
    }
}
