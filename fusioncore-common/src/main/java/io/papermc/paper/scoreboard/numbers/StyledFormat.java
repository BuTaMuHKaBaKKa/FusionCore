// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.scoreboard.numbers;

import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.StyleBuilderApplicable;

/**
 * A scoreboard number format that applies a custom formatting to the score number.
 */
public interface StyledFormat extends NumberFormat, StyleBuilderApplicable {

    /**
     * The style that is being applied to the number in the score
     *
     * @return the style to apply
     */
    Style style();

}
