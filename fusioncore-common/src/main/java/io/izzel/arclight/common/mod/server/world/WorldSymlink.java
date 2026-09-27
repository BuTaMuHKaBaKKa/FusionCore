package io.izzel.arclight.common.mod.server.world;

import io.izzel.arclight.common.mod.server.ArclightServer;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.world.level.storage.DerivedLevelData;

public class WorldSymlink {

    public static void create(DerivedLevelData worldInfo, File dimensionFolder) {
        String name = worldInfo.getLevelName();
        Path source = new File(Bukkit.getWorldContainer(), name).toPath();
        Path dest = dimensionFolder.toPath();
        try {
            if (!Files.isSymbolicLink(source)) {
                if (Files.exists(source)) {
                    if (Files.isDirectory(dest) && Files.isSameFile(source, dest)) {
                        return; // a junction made on an earlier start
                    }
                    ArclightServer.LOGGER.warn("symlink-file-exist", source);
                    return;
                }
                Files.createSymbolicLink(source, dest);
            }
        } catch (UnsupportedOperationException e) {
            ArclightServer.LOGGER.warn("error-symlink", e);
        } catch (IOException e) {
            if (createJunction(source, dest)) {
                return;
            }
            // Fusion Core: one line, no stack trace: on Windows this needs developer mode or administrator rights, and the world names
            // (compat.symlink-world) work without the link; only paths through "<name>/" do not
            org.apache.logging.log4j.LogManager.getLogger("Fusion Core").warn("Could not link {} to {} ({}: {}); the world keeps its name, only the folder path by that name is missing",
                source, dest, e.getClass().getSimpleName(), e.getMessage());
        }
    }

    // Fusion Core: Windows allows symbolic links only with developer mode or administrator rights, but a directory junction needs
    // neither and works the same for a folder path
    private static boolean createJunction(Path source, Path dest) {
        if (!System.getProperty("os.name", "").toLowerCase(java.util.Locale.ROOT).startsWith("windows")) {
            return false;
        }
        try {
            Process process = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J", source.toAbsolutePath().toString(), dest.toAbsolutePath().toString())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            return process.waitFor() == 0 && Files.exists(source, java.nio.file.LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
