package games.brennan.adventureitemstats.internal;

import com.mojang.logging.LogUtils;
import games.brennan.adventureitemstats.mixin.RangedAttributeAccessor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.slf4j.Logger;

/**
 * Raises the vanilla ceilings on {@code generic.armor} (30.0) and
 * {@code generic.armor_toughness} (20.0) so AIS's rolled progression bonus is
 * actually granted instead of being clamped away on read.
 *
 * <p>A full armor bar is 20 points, so vanilla's 30 lets armor grow to only 1.5
 * bars — with Dungeon Train's per-tier bonus that ceiling is reached around
 * difficulty tier 25, after which stronger armor stops doing anything while its
 * tooltip keeps promising more.
 *
 * <p><strong>This is a global change.</strong> Attribute ranges are game-wide,
 * so every armor item from every mod gets the raised ceiling, not just
 * AIS-rolled gear. Gated behind {@link AisConfig#raiseAttributeCaps()} for packs
 * that want vanilla clamping back.
 *
 * <p>Applied once at start-up from each loader entrypoint. Idempotent, and it
 * only ever raises a ceiling — see {@link #effectiveMax}.
 */
public final class AttributeCaps {

    private static final Logger LOGGER = LogUtils.getLogger();

    private AttributeCaps() {}

    /**
     * Raises the armor and toughness ceilings to the given values.
     *
     * <p>Never throws: attribute registry access at start-up is timing-sensitive
     * across loaders, and failing to raise a cap is a balance regression, not a
     * reason to take the game down. A failure is logged and the vanilla ceiling
     * stands.
     */
    public static void apply(double armorMax, double toughnessMax) {
        raise(Attributes.ARMOR.value(), armorMax, "generic.armor");
        raise(Attributes.ARMOR_TOUGHNESS.value(), toughnessMax, "generic.armor_toughness");
    }

    private static void raise(Attribute attribute, double requestedMax, String name) {
        try {
            if (!(attribute instanceof RangedAttribute)) {
                LOGGER.warn("[AdventureItemStats] {} is not a RangedAttribute — cap left at vanilla", name);
                return;
            }
            RangedAttributeAccessor accessor = (RangedAttributeAccessor) attribute;
            double currentMax = accessor.adventureitemstats$getMaxValue();
            double newMax = effectiveMax(currentMax, requestedMax);
            if (newMax == currentMax) {
                return;
            }
            accessor.adventureitemstats$setMaxValue(newMax);
            LOGGER.info("[AdventureItemStats] Raised {} ceiling {} -> {}", name, currentMax, newMax);
        } catch (Exception e) {
            LOGGER.warn("[AdventureItemStats] Could not raise the {} ceiling — leaving it at vanilla", name, e);
        }
    }

    /**
     * Pure: the ceiling to install, given what is already there.
     *
     * <p>Only ever raises. Another mod (or a second call from a different loader
     * entrypoint) may have already set a higher ceiling, and lowering it would
     * silently break that mod's gear — so the larger value always wins. This
     * also makes {@link #apply} idempotent. A non-finite request is ignored.
     * Package-visible for unit testing.
     */
    static double effectiveMax(double currentMax, double requestedMax) {
        if (!Double.isFinite(requestedMax)) {
            return currentMax;
        }
        return Math.max(currentMax, requestedMax);
    }
}
