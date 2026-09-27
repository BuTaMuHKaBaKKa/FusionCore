package io.izzel.arclight.installer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Mirrors {

    private static final String[] MAVEN_REPO = {
        "https://repo.maven.apache.org/maven2/",
        "https://maven.neoforged.net/releases/",
        "https://maven.minecraftforge.net/",
        "https://repo.spongepowered.org/repository/maven-public/",
        "https://arclight.hypertention.cn/"
    };

    // Fusion Core: official endpoint first; the third-party mirror is only a fallback.
    private static final String MOJANG_OFFICIAL = "https://piston-meta.mojang.com";
    private static final String MOJANG_THIRD_PARTY = "https://mojmirror.hypertention.cn";

    private static final String[] MOJANG_MIRROR = {
        MOJANG_OFFICIAL,
        MOJANG_THIRD_PARTY
    };

    private static final String VERSION_MANIFEST = "%s/mc/game/version_manifest.json";

    public static String[] getMavenRepo() {
        return MAVEN_REPO;
    }

    public static List<Map.Entry<String, String>> getVersionManifest() {
        return Arrays.stream(MOJANG_MIRROR).map(it -> Map.entry(it, VERSION_MANIFEST.formatted(it)))
            .collect(Collectors.toList());
    }

    public static String mapMojangMirror(String url, String mirror) {
        if (mirror.equals(MOJANG_OFFICIAL)) {
            return url;
        }
        return url.replace("https://launcher.mojang.com", mirror)
            .replace("https://launchermeta.mojang.com", mirror)
            .replace("https://piston-meta.mojang.com", mirror)
            .replace("https://piston-data.mojang.com", mirror);
    }

    public static boolean isMirrorUrl(String url) {
        return url.startsWith(MOJANG_THIRD_PARTY);
    }
}
