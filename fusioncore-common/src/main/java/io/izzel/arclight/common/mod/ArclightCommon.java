package io.izzel.arclight.common.mod;

import com.google.common.graph.Graph;

import java.util.List;
import java.util.Set;

public class ArclightCommon {

    public interface Api {

        byte[] platformRemapClass(byte[] cl);

        boolean isModLoaded(String modid);

        /**
         * This is here because Fabric and NeoForge use different version of Guava.
         * The signature for this method changed between these versions, leading
         * to NoSuchMethodError. It is recommended to check this method and remove
         * it if necessary when upgrading.
         */
        <T> Set<T> guavaReachableNodes(Graph<T> graph, T node);

        /**
         * Fusion Core: mod loader facts for the {@code /fusioncore} diagnostic command.
         * Defaulted so a platform that has not implemented them still compiles.
         */
        default String loaderName() {
            return "unknown";
        }

        default String loaderVersion() {
            return "unknown";
        }

        /**
         * @return {@code "<modid> <version>"} for every loaded mod, sorted by mod id.
         */
        default List<String> loadedMods() {
            return List.of();
        }
    }

    private static Api instance;

    public static Api api() {
        return instance;
    }

    public static void setInstance(Api instance) {
        ArclightCommon.instance = instance;
    }
}
