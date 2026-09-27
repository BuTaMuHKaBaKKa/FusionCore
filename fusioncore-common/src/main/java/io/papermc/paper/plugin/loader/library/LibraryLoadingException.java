// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.plugin.loader.library;

/**
 * Indicates that an exception has occured while loading a library.
 */
public class LibraryLoadingException extends RuntimeException {

    public LibraryLoadingException(String s) {
        super(s);
    }

    public LibraryLoadingException(String s, Exception e) {
        super(s, e);
    }
}
