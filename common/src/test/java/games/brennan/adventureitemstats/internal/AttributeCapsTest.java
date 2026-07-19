package games.brennan.adventureitemstats.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure-logic tests for {@link AttributeCaps#effectiveMax}. The mixin write
 * itself needs a running game and is covered by in-game verification; this
 * pins the "only ever raise" contract that keeps the call idempotent and
 * keeps AIS from stomping another mod's higher ceiling.
 */
class AttributeCapsTest {

    private static final double EPS = 1.0e-9;

    /** Vanilla generic.armor: max 30.0. The case that motivated the change. */
    @Test
    void raisesVanillaArmorCeiling() {
        assertEquals(1024.0, AttributeCaps.effectiveMax(30.0, 1024.0), EPS);
    }

    @Test
    void neverLowersAnExistingCeiling() {
        assertEquals(2048.0, AttributeCaps.effectiveMax(2048.0, 1024.0), EPS);
    }

    /** A second call from another loader entrypoint must be a no-op. */
    @Test
    void isIdempotent() {
        double once = AttributeCaps.effectiveMax(30.0, 1024.0);
        assertEquals(once, AttributeCaps.effectiveMax(once, 1024.0), EPS);
    }

    @Test
    void equalValuesAreUnchanged() {
        assertEquals(1024.0, AttributeCaps.effectiveMax(1024.0, 1024.0), EPS);
    }

    @Test
    void ignoresNonFiniteRequests() {
        assertEquals(30.0, AttributeCaps.effectiveMax(30.0, Double.NaN), EPS);
        assertEquals(30.0, AttributeCaps.effectiveMax(30.0, Double.POSITIVE_INFINITY), EPS);
    }
}
