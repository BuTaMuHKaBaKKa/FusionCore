// Fusion Core: Paper API 1.21.1 as published in paper-api sources; only the jspecify annotations are swapped for JetBrains ones.
package io.papermc.paper.scoreboard.numbers;

record BlankFormatImpl() implements NumberFormat {
    public static final BlankFormatImpl INSTANCE = new BlankFormatImpl();
}
