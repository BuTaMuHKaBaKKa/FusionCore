// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public class CachedSizeConcurrentLinkedQueue<E> extends ConcurrentLinkedQueue<E> {

    private final LongAdder cachedSize = new LongAdder();

    @Override
    public boolean add(final E e) {
        final boolean result = super.add(e);
        if (result) {
            this.cachedSize.increment();
        }
        return result;
    }

    @Override
    public @Nullable E poll() {
        final E result = super.poll();
        if (result != null) {
            this.cachedSize.decrement();
        }
        return result;
    }

    @Override
    public int size() {
        return this.cachedSize.intValue();
    }
}
