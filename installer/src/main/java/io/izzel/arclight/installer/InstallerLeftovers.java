package io.izzel.arclight.installer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** Fusion Core: removes files the (Neo)Forge installer leaves behind, unless they were edited. */
final class InstallerLeftovers {

    private static final Pattern INSTALLER_JAR = Pattern.compile("(neo)?forge-[0-9A-Za-z.+\\-]+-installer\\.jar");
    private static final Pattern LAUNCH_LINE = Pattern.compile("java @user_jvm_args\\.txt @libraries/\\S+_args\\.txt (%\\*|\"\\$@\")");

    private InstallerLeftovers() {
    }

    static void clean() {
        List<String> removed = new ArrayList<>();
        Path dir = Paths.get(".");
        remove(Paths.get("run.bat"), InstallerLeftovers::isLaunchScript, removed);
        remove(Paths.get("run.sh"), InstallerLeftovers::isLaunchScript, removed);
        remove(Paths.get("user_jvm_args.txt"), InstallerLeftovers::onlyComments, removed);
        remove(Paths.get("installer.log"), lines -> !lines.isEmpty() && lines.get(0).startsWith("JVM info:"), removed);
        remove(Paths.get(".fusioncore", "installer_stripped.jar"), null, removed);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dir, "*-installer.jar")) {
            for (Path file : files) {
                if (INSTALLER_JAR.matcher(file.getFileName().toString()).matches()) {
                    remove(file, null, removed);
                }
            }
        } catch (IOException ignored) {
        }
        if (!removed.isEmpty()) {
            System.out.println("Removed files left by the loader installer: " + String.join(", ", removed));
        }
    }

    private interface Check {
        boolean test(List<String> lines);
    }

    private static void remove(Path file, Check check, List<String> removed) {
        try {
            if (!Files.isRegularFile(file)) {
                return;
            }
            if (check != null && !check.test(Files.readAllLines(file, StandardCharsets.ISO_8859_1))) {
                return;
            }
            Files.delete(file);
            removed.add(file.toString().replace('\\', '/').replaceFirst("^\\./", ""));
        } catch (IOException ignored) {
        }
    }

    /** The installer's run.bat/run.sh: comments, {@code @echo off}, {@code pause} and its unedited java line */
    private static boolean isLaunchScript(List<String> lines) {
        boolean launch = false;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("REM") || line.equals("@echo off") || line.equals("pause")) {
                continue;
            }
            if (LAUNCH_LINE.matcher(line).matches()) {
                launch = true;
                continue;
            }
            return false;
        }
        return launch;
    }

    private static boolean onlyComments(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return false;
            }
        }
        return true;
    }
}
