package io.izzel.arclight.i18n;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Fusion Core: the server's own files are named after Fusion Core. Servers that ran an earlier build have them under the
 * earlier names (VitaCore, Arclight); those are renamed once, on the first start that looks for them.
 */
public final class FusionCoreFiles {

    private FusionCoreFiles() {
    }

    /** {@code fusioncore.conf}, renamed from {@code vitacore.conf} or {@code arclight.conf} if only one of them exists */
    public static Path config() {
        return migrate(Paths.get("fusioncore.conf"), Paths.get("vitacore.conf"), Paths.get("arclight.conf"));
    }

    private static synchronized Path migrate(Path current, Path... previous) {
        if (Files.exists(current)) {
            return current;
        }
        for (Path old : previous) {
            if (Files.exists(old)) {
                try {
                    Files.move(old, current);
                    System.out.println("Fusion Core: renamed " + old + " to " + current);
                } catch (IOException e) {
                    System.err.println("Fusion Core: could not rename " + old + " to " + current + ": " + e);
                    return old;
                }
                break;
            }
        }
        return current;
    }
}
