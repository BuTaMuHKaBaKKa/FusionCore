package io.izzel.arclight.common.mod.util;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import net.minecraft.SharedConstants;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Fusion Core: Paper's {@code ServerBuildInfoImpl} with Fusion Core's brand and git commit.
 * {@link #isBrandCompatible} also accepts Paper's brand.
 */
public final class FusionCoreBuildInfo implements ServerBuildInfo {

    public static final FusionCoreBuildInfo INSTANCE = new FusionCoreBuildInfo();

    private static final Key BRAND_FUSIONCORE_ID = Key.key("fusioncore", "fusioncore");
    private static final String BUILD_DEV = "DEV";

    private final Optional<String> gitCommit;
    private final Instant buildTime = Instant.ofEpochMilli(ManagementFactory.getRuntimeMXBean().getStartTime());

    private FusionCoreBuildInfo() {
        String version = FusionCoreBuildInfo.class.getPackage().getImplementationVersion();
        int plus = version == null ? -1 : version.lastIndexOf('+');
        this.gitCommit = plus < 0 || plus == version.length() - 1 ? Optional.empty() : Optional.of(version.substring(plus + 1));
    }

    @Override
    public Key brandId() {
        return BRAND_FUSIONCORE_ID;
    }

    @Override
    public boolean isBrandCompatible(Key brandId) {
        return brandId.equals(BRAND_FUSIONCORE_ID) || brandId.equals(BRAND_PAPER_ID);
    }

    @Override
    public String brandName() {
        return "Fusion Core";
    }

    @Override
    public String minecraftVersionId() {
        return SharedConstants.getCurrentVersion().getId();
    }

    @Override
    public String minecraftVersionName() {
        return SharedConstants.getCurrentVersion().getName();
    }

    @Override
    public OptionalInt buildNumber() {
        return OptionalInt.empty();
    }

    @Override
    public Instant buildTime() {
        return this.buildTime;
    }

    @Override
    public Optional<String> gitBranch() {
        return Optional.empty();
    }

    @Override
    public Optional<String> gitCommit() {
        return this.gitCommit;
    }

    @Override
    public String asString(StringRepresentation representation) {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.minecraftVersionId());
        sb.append('-');
        if (this.buildNumber().isPresent()) {
            sb.append(this.buildNumber().getAsInt());
        } else {
            sb.append(BUILD_DEV);
        }
        final boolean hasGitBranch = this.gitBranch().isPresent();
        final boolean hasGitCommit = this.gitCommit.isPresent();
        if (hasGitBranch || hasGitCommit) {
            sb.append('-');
        }
        if (hasGitBranch && representation == StringRepresentation.VERSION_FULL) {
            sb.append(this.gitBranch().get());
            if (hasGitCommit) {
                sb.append('@');
            }
        }
        if (hasGitCommit) {
            sb.append(this.gitCommit.get());
        }
        if (representation == StringRepresentation.VERSION_FULL) {
            sb.append(' ');
            sb.append('(');
            sb.append(this.buildTime.truncatedTo(ChronoUnit.SECONDS));
            sb.append(')');
        }
        return sb.toString();
    }
}
