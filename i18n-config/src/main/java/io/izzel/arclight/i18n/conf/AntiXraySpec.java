package io.izzel.arclight.i18n.conf;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

import java.util.List;
import java.util.Map;

// Fusion Core: Paper's anticheat.anti-xray, defaults as in Paper; "worlds" overrides keys for one world by its folder name
@ConfigSerializable
public class AntiXraySpec {

    @Setting("enabled")
    private boolean enabled = false;

    @Setting("engine-mode")
    private int engineMode = 1;

    @Setting("max-block-height")
    private int maxBlockHeight = 64;

    @Setting("update-radius")
    private int updateRadius = 2;

    @Setting("lava-obscures")
    private boolean lavaObscures = false;

    @Setting("use-permission")
    private boolean usePermission = false;

    @Setting("hidden-blocks")
    private List<String> hiddenBlocks;

    @Setting("replacement-blocks")
    private List<String> replacementBlocks;

    @Setting("worlds")
    private Map<String, World> worlds;

    /** The settings of one world: its override where given, the common value otherwise */
    public Resolved resolve(String worldName) {
        World w = worlds == null ? null : worlds.get(worldName);
        if (w == null && worlds != null && worldName.lastIndexOf('/') >= 0) {
            w = worlds.get(worldName.substring(worldName.lastIndexOf('/') + 1));
        }
        if (w == null) {
            w = new World();
        }
        return new Resolved(
            w.enabled != null ? w.enabled : enabled,
            w.engineMode != null ? w.engineMode : engineMode,
            w.maxBlockHeight != null ? w.maxBlockHeight : maxBlockHeight,
            w.updateRadius != null ? w.updateRadius : updateRadius,
            w.lavaObscures != null ? w.lavaObscures : lavaObscures,
            w.usePermission != null ? w.usePermission : usePermission,
            w.hiddenBlocks != null ? w.hiddenBlocks : hiddenBlocks == null ? List.of() : hiddenBlocks,
            w.replacementBlocks != null ? w.replacementBlocks : replacementBlocks == null ? List.of() : replacementBlocks
        );
    }

    public record Resolved(boolean enabled, int engineMode, int maxBlockHeight, int updateRadius, boolean lavaObscures,
                           boolean usePermission, List<String> hiddenBlocks, List<String> replacementBlocks) {
    }

    @ConfigSerializable
    public static class World {

        @Setting("enabled")
        private Boolean enabled;

        @Setting("engine-mode")
        private Integer engineMode;

        @Setting("max-block-height")
        private Integer maxBlockHeight;

        @Setting("update-radius")
        private Integer updateRadius;

        @Setting("lava-obscures")
        private Boolean lavaObscures;

        @Setting("use-permission")
        private Boolean usePermission;

        @Setting("hidden-blocks")
        private List<String> hiddenBlocks;

        @Setting("replacement-blocks")
        private List<String> replacementBlocks;
    }
}
