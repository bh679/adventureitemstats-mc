package games.brennan.adventureitemstats.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure-logic tests for {@link StatsModifier#finalAmount} — the per-attribute
 * amount math that backs {@link StatsModifier#applyStats(net.minecraft.world.item.ItemStack,
 * net.minecraft.util.RandomSource, double)}. The attribute classification and
 * data-component plumbing need a Minecraft bootstrap and are exercised in-game;
 * here we lock down the arithmetic (effective-value scaling + flat primary bonus).
 */
class StatsModifierTest {

    private static final double EPS = 1.0e-9;

    @Test
    void identityFactor_noBonus_returnsOriginalAmount() {
        // factor 1.0, bonus 0 → modifier amount unchanged, independent of playerBase
        assertEquals(7.0, StatsModifier.finalAmount(1.0, 7.0, 1.0, 0.0, true), EPS);
        assertEquals(2.0, StatsModifier.finalAmount(0.0, 2.0, 1.0, 0.0, false), EPS);
    }

    @Test
    void factorScalesEffectiveValue_notRawAmount() {
        // playerBase 1.0, original 6.0, factor 1.5 → effective (1+6)*1.5 = 10.5 → amount 9.5
        assertEquals(9.5, StatsModifier.finalAmount(1.0, 6.0, 1.5, 0.0, true), EPS);
    }

    @Test
    void primaryStat_addsFlatBonusOnTop() {
        // attack damage: (1+6)*1.0 - 1 = 6.0, + 1.5 bonus = 7.5
        assertEquals(7.5, StatsModifier.finalAmount(1.0, 6.0, 1.0, 1.5, true), EPS);
        // bonus stacks on top of a non-trivial factor too
        assertEquals(10.0, StatsModifier.finalAmount(1.0, 6.0, 1.5, 0.5, true), EPS);
    }

    @Test
    void primaryBonus_isNotClamped() {
        // unlike the σ roll, the deliberate bonus has no upper bound
        assertEquals(106.0, StatsModifier.finalAmount(1.0, 6.0, 1.0, 100.0, true), EPS);
    }

    @Test
    void secondaryStat_ignoresBonus() {
        // attack speed (playerBase 4.0) / armor toughness: bonus must NOT apply
        assertEquals(-2.4, StatsModifier.finalAmount(4.0, -2.4, 1.0, 1.5, false), EPS);
        assertEquals(2.0, StatsModifier.finalAmount(0.0, 2.0, 1.0, 5.0, false), EPS);
    }

    @Test
    void fixedFactors_sameFactorOnEveryStat() {
        var f = StatsModifier.fixedFactors(1.2);
        assertEquals(1.2, f.damage(), EPS);
        assertEquals(1.2, f.speed(), EPS);
        assertEquals(1.2, f.armor(), EPS);
        assertEquals(1.2, f.toughness(), EPS);
    }

    @Test
    void fixedFactors_clampedLikeARoll() {
        assertEquals(1.5, StatsModifier.fixedFactors(3.0).armor(), EPS);
        assertEquals(0.5, StatsModifier.fixedFactors(0.1).damage(), EPS);
    }

    @Test
    void bestRollPlusBonus_ironChestplateAndSword() {
        // iron chestplate (6 armor) at the 1.5 cap with a +2 bonus → 11
        double f = StatsModifier.fixedFactors(1.5).armor();
        assertEquals(11.0, StatsModifier.finalAmount(0.0, 6.0, f, 2.0, true), EPS);
        // iron sword (+5 damage, player base 1) at 1.5 → (1+5)*1.5 - 1 = 8
        assertEquals(8.0, StatsModifier.finalAmount(1.0, 5.0, f, 0.0, true), EPS);
    }
}
