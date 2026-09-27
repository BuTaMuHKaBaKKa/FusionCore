// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper.util;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class SneakyThrow {

    public static void sneaky(final Throwable exception) {
        SneakyThrow.throwSneaky(exception);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwSneaky(final Throwable exception) throws T {
        throw (T) exception;
    }

}
