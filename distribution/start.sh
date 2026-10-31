#!/usr/bin/env sh
set -eu
cd -- "$(dirname -- "$0")"
FUSIONCORE_JAR=fusioncore-neoforge-1.21.1-1.0.jar
FUSIONCORE_MEMORY="${FUSIONCORE_MEMORY:-4G}"
# Set only the maximum heap; Java chooses the initial heap automatically.
if [ -x runtime/bin/java ]; then
    FUSIONCORE_JAVA=./runtime/bin/java
elif [ -n "${JAVA_HOME:-}" ]; then
    FUSIONCORE_JAVA="$JAVA_HOME/bin/java"
else
    FUSIONCORE_JAVA=java
fi
if ! "$FUSIONCORE_JAVA" -XshowSettings:properties -version 2>&1 | grep -q 'java.specification.version = 21$'; then
    printf 'Java 21 is required: %s is not Java 21. Set JAVA_HOME or install 64-bit Java 21.\n' "$FUSIONCORE_JAVA" >&2
    exit 1
fi
if [ ! -f "$FUSIONCORE_JAR" ]; then
    printf 'Missing %s next to start.sh.\n' "$FUSIONCORE_JAR" >&2
    exit 1
fi
printf 'Starting Fusion Core 1.0 - Minecraft 1.21.1 / NeoForge 21.1.250\n'
printf 'Maximum Java heap: %s\n' "$FUSIONCORE_MEMORY"
exec "$FUSIONCORE_JAVA" "-Xmx$FUSIONCORE_MEMORY" -Dfile.encoding=UTF-8 ${FUSIONCORE_JAVA_OPTS:-} -jar "$FUSIONCORE_JAR" nogui "$@"
