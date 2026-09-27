package io.izzel.arclight.installer;

import java.util.HashMap;
import java.util.Map;

public class InstallInfo {

    public Installer installer;
    public Map<String, String> libraries;
    public Map<String, String> fabricExtra;
    public Map<String, String> neoforgeExtra;

    public static class Installer {

        public String minecraft;
        public String forge;
        public String forgeHash;
        public String neoforge;
        public String neoforgeHash;
        public String fabricLoader;
        public String fabricLoaderHash;
    }

    public Map<String, String> fabricDeps() {
        var map = new HashMap<>(this.libraries);
        map.putAll(this.fabricExtra);
        return map;
    }

    // Fusion Core: NeoForge's jline-terminal 3.20.0 cannot use the 3.12.1 Jansi provider; it gets
    // the matching JNA provider instead (see installerLibsNeoforge in bootstrap/build.gradle).
    public Map<String, String> neoforgeDeps() {
        var map = new HashMap<>(this.libraries);
        map.keySet().removeIf(it -> it.startsWith("org.jline:jline-terminal-jansi:"));
        map.putAll(this.neoforgeExtra);
        return map;
    }
}
