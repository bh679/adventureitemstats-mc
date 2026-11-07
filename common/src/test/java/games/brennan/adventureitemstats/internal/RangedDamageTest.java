package games.brennan.adventureitemstats.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pure-logic tests for {@link RangedDamage}. Damage at hit time is {@code speed * base damage}, so
 * these check the number a shot ends up dealing. Registration and real arrows are exercised in-game.
 */
class RangedDamageTest {

    private static final double EPS = 1.0e-9;

    /** What vanilla deals on hit, before its round-up and crits. */
    private static double dealt(double speed, double baseDamage, double stat, double nominal, double referenceSpeed) {
        double gain = RangedDamage.baseDamageGain(stat, nominal, referenceSpeed);
        return speed * RangedDamage.boostedBaseDamage(baseDamage, gain);
    }

    @Test
    void plainWeapon_gainsNothing() {
        assertEquals(0.0, RangedDamage.baseDamageGain(RangedDamage.NOMINAL_BOW, RangedDamage.NOMINAL_BOW, 3.0), EPS);
        assertEquals(0.0, RangedDamage.baseDamageGain(RangedDamage.NOMINAL_CROSSBOW, RangedDamage.NOMINAL_CROSSBOW, 3.15), EPS);
    }

    @Test
    void nominal_isBowOrCrossbowFullPowerShot() {
        assertEquals(6.0, RangedDamage.nominal(false), EPS);
        assertEquals(7.0, RangedDamage.nominal(true), EPS);
    }

    @Test
    void fullDraw_dealsTheTooltipNumber() {
        assertEquals(16.0, dealt(3.0, 2.0, 16.0, RangedDamage.NOMINAL_BOW, RangedDamage.FULL_DRAW_SPEED), EPS);
    }

    @Test
    void partialDraw_earnsAProportionalShare() {
        // half the speed: half of vanilla's 6 and half of the +10
        assertEquals(8.0, dealt(1.5, 2.0, 16.0, RangedDamage.NOMINAL_BOW, RangedDamage.FULL_DRAW_SPEED), EPS);
    }

    @Test
    void mobShot_landsTheWholeGain_atItsOwnSpeed() {
        // a skeleton: speed 1.6, base ~3.2 (vanilla ~5.1). A bow rated 16 adds the full +10.
        assertEquals(1.6 * 3.2 + 10.0, dealt(1.6, 3.2, 16.0, RangedDamage.NOMINAL_BOW, 1.6), EPS);
    }

    @Test
    void poorRoll_isWeakerButNeverBelowZero() {
        assertEquals(4.8, dealt(3.0, 2.0, 4.8, RangedDamage.NOMINAL_BOW, RangedDamage.FULL_DRAW_SPEED), EPS);
        assertEquals(0.0, RangedDamage.boostedBaseDamage(0.5, -2.0), EPS);
    }
}
