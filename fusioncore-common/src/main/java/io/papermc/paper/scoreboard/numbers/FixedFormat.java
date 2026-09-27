// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.scoreboard.numbers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

/**
 * A scoreboard number format that replaces the score number with a chat component.
 */
public interface FixedFormat extends NumberFormat, ComponentLike {

    /**
     * The component shown instead of the number for a score
     *
     * @return the chat component
     */
    Component component();

}
