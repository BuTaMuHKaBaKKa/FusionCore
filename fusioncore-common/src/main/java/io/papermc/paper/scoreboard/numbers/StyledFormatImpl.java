// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.scoreboard.numbers;

import net.kyori.adventure.text.format.Style;

record StyledFormatImpl(Style style) implements StyledFormat {
    static final StyledFormat NO_STYLE = new StyledFormatImpl(Style.empty());

    @Override
    public void styleApply(final Style.Builder style) {
        style.merge(this.style);
    }
}
