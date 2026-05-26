package games.brennan.adventureitemstats.api;

import net.minecraft.world.item.ItemStack;
import net.minecraft.util.RandomSource;

/**
 * Public entry point for the Adventure Item Stats mod.
 *
 * <p>Other mods that have their own loot system can depend on this
 * mod and call {@link #applyStats(ItemStack, RandomSource)} on
 * freshly-rolled items. Naturally-spawned items are already routed
 * through this method by the bundled
 * {@link games.brennan.adventureitemstats.mixin.LootTableMixin}.</p>
 *
 * <p>v0.1 is a stub — calls are no-ops. The Gaussian variation logic
 * lands in a follow-up session (see {@code CLAUDE.md} roadmap).</p>
 */
public final class StatsModifier {

    private StatsModifier() {}

    /**
     * Apply a Gaussian-distributed multiplier to the item's base stats
     * (attack damage, attack speed, armor + toughness) and write the
     * result to the stack's {@code ATTRIBUTE_MODIFIERS} data component.
     *
     * <p>Stub: no-op in v0.1. The signature is the long-term API
     * contract — pass a seeded {@link RandomSource} for deterministic
     * rolls per position/seed.</p>
     */
    public static void applyStats(ItemStack stack, RandomSource rng) {
        // intentionally empty — see roadmap
    }
}
