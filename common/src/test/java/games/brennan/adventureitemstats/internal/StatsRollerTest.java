package games.brennan.adventureitemstats.internal;

import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link StatsRoller}. These exercise the math
 * directly via {@link StatsRoller#computeWeaponFactors} and
 * {@link StatsRoller#computeArmorFactors}, plus one integration test
 * against the real {@link RandomSource#create(long)} to confirm the
 * skip-flag path consumes no entropy.
 */
class StatsRollerTest {

    private static final double EPS = 1.0e-9;

    @Test
    void clampFactor_passesThroughInBounds() {
        assertEquals(1.0, StatsRoller.clampFactor(1.0), EPS);
        assertEquals(0.5, StatsRoller.clampFactor(0.5), EPS);
        assertEquals(1.5, StatsRoller.clampFactor(1.5), EPS);
    }

    @Test
    void clampFactor_clampsOutOfBounds() {
        assertEquals(StatsRoller.MIN_FACTOR, StatsRoller.clampFactor(-2.0), EPS);
        assertEquals(StatsRoller.MIN_FACTOR, StatsRoller.clampFactor(0.0), EPS);
        assertEquals(StatsRoller.MAX_FACTOR, StatsRoller.clampFactor(5.0), EPS);
    }

    @Test
    void zeroGaussian_yieldsIdentityFactors() {
        double[] weapon = StatsRoller.computeWeaponFactors(0.0, 0.0, StatsRoller.SIGMA);
        double[] armor  = StatsRoller.computeArmorFactors(0.0, 0.0, StatsRoller.SIGMA);
        assertEquals(1.0, weapon[0], EPS);
        assertEquals(1.0, weapon[1], EPS);
        assertEquals(1.0, armor[0], EPS);
        assertEquals(1.0, armor[1], EPS);
    }

    @Test
    void positiveQuality_makesWeaponHarderAndFaster() {
        // pure quality axis: g_q > 0, g_t = 0 → both effective damage AND effective speed rise
        double[] weapon = StatsRoller.computeWeaponFactors(1.0, 0.0, StatsRoller.SIGMA);
        assertTrue(weapon[0] > 1.0, "damage factor should rise with quality; got " + weapon[0]);
        assertTrue(weapon[1] > 1.0, "speed factor should rise with quality (faster swing); got " + weapon[1]);
    }

    @Test
    void positiveTradeoff_favorsDamageOverSpeed() {
        // pure tradeoff axis: g_t > 0, g_q = 0 → harder hit (damage up) AND slower swing (speed down)
        double[] weapon = StatsRoller.computeWeaponFactors(0.0, 1.0, StatsRoller.SIGMA);
        assertTrue(weapon[0] > 1.0, "tradeoff should boost damage; got " + weapon[0]);
        assertTrue(weapon[1] < 1.0, "positive tradeoff should drop effective speed (slower swing); got " + weapon[1]);
    }

    @Test
    void positiveQuality_makesArmorAndToughnessRise() {
        double[] armor = StatsRoller.computeArmorFactors(1.0, 0.0, StatsRoller.SIGMA);
        assertTrue(armor[0] > 1.0, "armor factor should rise with quality; got " + armor[0]);
        assertTrue(armor[1] > 1.0, "toughness factor should rise with quality; got " + armor[1]);
    }

    @Test
    void positiveTradeoff_favorsArmorOverToughness() {
        double[] armor = StatsRoller.computeArmorFactors(0.0, 1.0, StatsRoller.SIGMA);
        assertTrue(armor[0] > 1.0, "armor should rise with positive tradeoff; got " + armor[0]);
        assertTrue(armor[1] < 1.0, "toughness should fall with positive tradeoff; got " + armor[1]);
    }

    @Test
    void extremeGaussian_factorsClampToBounds() {
        double[] weapon = StatsRoller.computeWeaponFactors(50.0, 50.0, StatsRoller.SIGMA);
        assertEquals(StatsRoller.MAX_FACTOR, weapon[0], EPS);
        double[] armor = StatsRoller.computeArmorFactors(-50.0, -50.0, StatsRoller.SIGMA);
        assertEquals(StatsRoller.MIN_FACTOR, armor[0], EPS);
    }

    @Test
    void sample_skipFlagsConsumeNoEntropy() {
        // If we skip both groups, two back-to-back sample() calls with the same
        // seed should give identical factors (no RNG advancement).
        RandomSource a = RandomSource.create(42L);
        RandomSource b = RandomSource.create(42L);
        StatsRoller.Factors first  = StatsRoller.sample(a, false, false, StatsRoller.SIGMA);
        StatsRoller.Factors second = StatsRoller.sample(b, false, false, StatsRoller.SIGMA);
        assertEquals(first, second);
        // and both should be all-1.0 since nothing was rolled
        assertEquals(new StatsRoller.Factors(1.0, 1.0, 1.0, 1.0), first);

        // If we then roll weapon-only on `a`, it advances exactly 2 Gaussians and
        // the next armor-only roll on `a` matches an armor-only roll on a fresh
        // seed that's first consumed by 2 Gaussians.
        RandomSource c = RandomSource.create(42L);
        c.nextGaussian();
        c.nextGaussian();
        StatsRoller.Factors weaponSkip = StatsRoller.sample(a, true,  false, StatsRoller.SIGMA);
        StatsRoller.Factors armorOnly  = StatsRoller.sample(a, false, true,  StatsRoller.SIGMA);
        StatsRoller.Factors armorPure  = StatsRoller.sample(c, false, true,  StatsRoller.SIGMA);
        assertNotNull(weaponSkip);
        assertEquals(armorOnly.armor(), armorPure.armor(), EPS);
        assertEquals(armorOnly.toughness(), armorPure.toughness(), EPS);
    }

    @Test
    void sample_allFactorsAlwaysWithinClampBounds() {
        // Stress: 10k samples should all stay within [MIN_FACTOR, MAX_FACTOR].
        RandomSource rng = RandomSource.create(0xDEADBEEFL);
        for (int i = 0; i < 10_000; i++) {
            StatsRoller.Factors f = StatsRoller.sample(rng, true, true, StatsRoller.SIGMA);
            assertInRange(f.damage(), "damage");
            assertInRange(f.speed(), "speed");
            assertInRange(f.armor(), "armor");
            assertInRange(f.toughness(), "toughness");
        }
    }

    private static void assertInRange(double v, String label) {
        assertTrue(v >= StatsRoller.MIN_FACTOR && v <= StatsRoller.MAX_FACTOR,
            label + " out of bounds: " + v);
    }
}
