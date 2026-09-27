package io.izzel.arclight.common.mod.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.izzel.arclight.common.bridge.core.world.item.trading.MerchantOfferBridge;
import net.minecraft.world.item.trading.MerchantOffer;

/** Fusion Core: Paper's ignore-discounts flag on villager offers, saved as {@code Paper.IgnoreDiscounts}. */
public final class FusionCoreMerchantOffers {

    private static final String IGNORE_DISCOUNTS = "Paper.IgnoreDiscounts";

    private FusionCoreMerchantOffers() {
    }

    public static Codec<MerchantOffer> withIgnoreDiscounts(Codec<MerchantOffer> codec) {
        return new Codec<>() {
            @Override
            public <T> DataResult<T> encode(MerchantOffer offer, DynamicOps<T> ops, T prefix) {
                DataResult<T> result = codec.encode(offer, ops, prefix);
                if (!((MerchantOfferBridge) offer).fusioncore$ignoreDiscounts()) {
                    return result;
                }
                return result.flatMap(map -> ops.mergeToMap(map, ops.createString(IGNORE_DISCOUNTS), ops.createBoolean(true)));
            }

            @Override
            public <T> DataResult<Pair<MerchantOffer, T>> decode(DynamicOps<T> ops, T input) {
                return codec.decode(ops, input).map(pair -> {
                    boolean ignore = ops.get(input, IGNORE_DISCOUNTS).flatMap(ops::getBooleanValue).result().orElse(false);
                    ((MerchantOfferBridge) pair.getFirst()).fusioncore$setIgnoreDiscounts(ignore);
                    return pair;
                });
            }

            @Override
            public String toString() {
                return codec + "[Fusion Core: " + IGNORE_DISCOUNTS + "]";
            }
        };
    }

    /** Paper skips offers that ignore discounts when a villager applies reputation and Hero of the Village prices. */
    public static boolean ignoresDiscounts(MerchantOffer offer) {
        return ((MerchantOfferBridge) offer).fusioncore$ignoreDiscounts();
    }
}
