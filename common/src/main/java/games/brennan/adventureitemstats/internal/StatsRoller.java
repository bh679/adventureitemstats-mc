package games.brennan.adventureitemstats.internal;

import net.minecraft.util.RandomSource;

/**
 * Pure-math helper that turns a {@link RandomSource} into per-attribute
 * multipliers for the four stats Adventure Item Stats varies: attack
 * damage, attack speed, armor, armor toughness.
 *
 * <p>Two-axis model: per stat-group, we draw a {@code g_q} (quality) and
 * a {@code g_t} (tradeoff) sample from a standard normal. Each returned
 * factor is a multiplier on the <em>effective</em> attribute value (e.g.
 * a damage factor of 1.2 means the wielded sword does 20% more damage
 * than vanilla, not that the modifier amount is multiplied by 1.2).
 * Factors are combined so per-axis variance equals {@code σ²}:
 *
 * <pre>
 *   damage    = 1 + (g_q + g_t) * (σ / √2)    // &gt; 1 = harder hit
 *   speed     = 1 + (g_q - g_t) * (σ / √2)    // &gt; 1 = faster swing
 *   armor     = 1 + (g_q + g_t) * (σ / √2)    // &gt; 1 = more armor
 *   toughness = 1 + (g_q - g_t) * (σ / √2)    // &gt; 1 = more toughness
 * </pre>
 *
 * <p>So positive {@code g_q} = "good weapon" (both harder and faster),
 * positive {@code g_t} = "favors damage over speed" (harder and slower).
 * Same shape for armor: quality lifts both, tradeoff favors armor over
 * toughness.
 *
 * <p>Stateless and almost-Minecraft-free (only the {@link RandomSource}
 * interface is referenced) so the math is directly JUnit-testable via
 * {@link #computeWeaponFactors} and {@link #computeArmorFactors}.
 */
public final class StatsRoller {

    public static final double SIGMA = 0.20;
    public static final double MIN_FACTOR = 0.5;
    public static final double MAX_FACTOR = 1.5;
    public static final String NAMESPACE = "adventureitemstats";

    private static final double AXIS_WEIGHT = 1.0 / Math.sqrt(2.0);

    private StatsRoller() {}

    public record Factors(double damage, double speed, double armor, double toughness) {}

    /**
     * Draw factors for the requested stat groups. When a group flag is
     * {@code false}, that group's factors are returned as {@code 1.0}
     * and no entropy is consumed for it (so armor-only stacks don't
     * shift the RNG stream the way weapon-only stacks do).
     */
    public static Factors sample(RandomSource rng, boolean rollWeapon, boolean rollArmor, double sigma) {
        double damage = 1.0, speed = 1.0, armor = 1.0, toughness = 1.0;
        if (rollWeapon) {
            double[] w = computeWeaponFactors(rng.nextGaussian(), rng.nextGaussian(), sigma);
            damage = w[0];
            speed = w[1];
        }
        if (rollArmor) {
            double[] a = computeArmorFactors(rng.nextGaussian(), rng.nextGaussian(), sigma);
            armor = a[0];
            toughness = a[1];
        }
        return new Factors(damage, speed, armor, toughness);
    }

    /** Returns {@code [damageFactor, speedFactor]} — both interpreted as multipliers on the effective wielded value. */
    public static double[] computeWeaponFactors(double gq, double gt, double sigma) {
        double step = sigma * AXIS_WEIGHT;
        return new double[] {
            clampFactor(1.0 + (gq + gt) * step),
            clampFactor(1.0 + (gq - gt) * step)
        };
    }

    /** Returns {@code [armorFactor, toughnessFactor]} — both interpreted as multipliers on the effective worn value. */
    public static double[] computeArmorFactors(double gq, double gt, double sigma) {
        double step = sigma * AXIS_WEIGHT;
        return new double[] {
            clampFactor(1.0 + (gq + gt) * step),
            clampFactor(1.0 + (gq - gt) * step)
        };
    }

    public static double clampFactor(double f) {
        if (f < MIN_FACTOR) return MIN_FACTOR;
        if (f > MAX_FACTOR) return MAX_FACTOR;
        return f;
    }
}
