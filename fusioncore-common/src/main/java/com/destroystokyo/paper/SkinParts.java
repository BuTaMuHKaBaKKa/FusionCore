// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package com.destroystokyo.paper;

public interface SkinParts {

    boolean hasCapeEnabled();

    boolean hasJacketEnabled();

    boolean hasLeftSleeveEnabled();

    boolean hasRightSleeveEnabled();

    boolean hasLeftPantsEnabled();

    boolean hasRightPantsEnabled();

    boolean hasHatsEnabled();

    int getRaw();
}
