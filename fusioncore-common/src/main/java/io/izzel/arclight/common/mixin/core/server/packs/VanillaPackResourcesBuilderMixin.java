package io.izzel.arclight.common.mixin.core.server.packs;

import net.minecraft.server.packs.VanillaPackResourcesBuilder;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URI;

/** Fusion Core: accept {@code union:} asset URLs (NeoForge) without the "unexpected schema" warning. */
@Mixin(VanillaPackResourcesBuilder.class)
public class VanillaPackResourcesBuilderMixin {

    // The ROOT_DIR_BY_TYPE initializer is a lambda: matched by descriptor, as lambda names differ
    // between dev and runtime mappings.
    @Redirect(method = "*()Lcom/google/common/collect/ImmutableMap;",
        at = @At(value = "INVOKE", remap = false, target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
    private static void fusioncore$acceptUnionScheme(Logger logger, String message, Object uri) {
        if (!(uri instanceof URI u && "union".equals(u.getScheme()))) {
            logger.warn(message, uri);
        }
    }
}
