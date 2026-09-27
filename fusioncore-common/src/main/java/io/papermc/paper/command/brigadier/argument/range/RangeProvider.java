// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.command.brigadier.argument.range;

import com.google.common.collect.Range;
import org.jetbrains.annotations.ApiStatus;

/**
 * A provider for a range of numbers
 *
 * @param <T>
 * @see io.papermc.paper.command.brigadier.argument.ArgumentTypes
 */
@ApiStatus.Experimental
public sealed interface RangeProvider<T extends Comparable<?>> permits DoubleRangeProvider, IntegerRangeProvider {

    /**
     * Provides the given range.
     * @return range
     */
    Range<T> range();
}
