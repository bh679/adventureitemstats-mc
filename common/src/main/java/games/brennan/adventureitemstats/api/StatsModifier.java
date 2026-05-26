package games.brennan.adventureitemstats.api;

import games.brennan.adventureitemstats.internal.StatsRoller;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * Public entry point for the Adventure Item Stats mod.
 *
 * <p>Other mods that have their own loot system can depend on this
 * mod and call {@link #applyStats(ItemStack, RandomSource)} on
 * freshly-rolled items. Naturally-spawned items are already routed
 * through this method by the bundled
 * {@link games.brennan.adventureitemstats.mixin.LootTableMixin}.</p>
 *
 * <p>Applies a Gaussian-distributed multiplier to the stack's
 * <em>effective</em> attack damage, attack speed, armor and armor
 * toughness, writing the rolled modifier amounts back to the
 * {@code ATTRIBUTE_MODIFIERS} data component. Items without any of
 * the four target attributes are no-ops.</p>
 */
public final class StatsModifier {

    // Player-class attribute base values that vanilla MC adds on top of the
    // item's modifier amount. We scale the *effective* (player base + modifier)
    // value so σ=0.20 means ±20% on what the player feels in combat, not on
    // the raw modifier amount.
    private static final double PLAYER_BASE_ATTACK_DAMAGE = 1.0;
    private static final double PLAYER_BASE_ATTACK_SPEED  = 4.0;

    private StatsModifier() {}

    /**
     * Apply rolled stats to {@code stack}. See {@link StatsRoller} for
     * the two-axis distribution model.
     *
     * <p>No idempotency marker yet — the mixin fires once per vanilla
     * loot generation, so double-rolling only happens if a mod routes
     * already-rolled stacks back through {@code LootTable.getRandomItems}.
     * That risk is bounded by the σ clamp and will be eliminated in v0.3
     * via a CustomData marker.</p>
     */
    public static void applyStats(ItemStack stack, RandomSource rng) {
        ItemAttributeModifiers current = stack.getOrDefault(
            DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        List<ItemAttributeModifiers.Entry> entries = current.modifiers();
        if (entries.isEmpty()) return;

        boolean hasWeapon = false, hasArmor = false;
        for (ItemAttributeModifiers.Entry entry : entries) {
            Attribute attr = entry.attribute().value();
            if (attr == Attributes.ATTACK_DAMAGE.value() || attr == Attributes.ATTACK_SPEED.value()) {
                hasWeapon = true;
            } else if (attr == Attributes.ARMOR.value() || attr == Attributes.ARMOR_TOUGHNESS.value()) {
                hasArmor = true;
            }
        }
        if (!hasWeapon && !hasArmor) return;

        StatsRoller.Factors factors = StatsRoller.sample(rng, hasWeapon, hasArmor, StatsRoller.SIGMA);

        List<ItemAttributeModifiers.Entry> rebuilt = new ArrayList<>(entries.size());
        for (ItemAttributeModifiers.Entry entry : entries) {
            Attribute attr = entry.attribute().value();
            Double factor = factorFor(attr, factors);
            if (factor == null) {
                rebuilt.add(entry);
                continue;
            }
            AttributeModifier original = entry.modifier();
            double playerBase = playerBaseFor(attr);
            double rolledFinal = (playerBase + original.amount()) * factor;
            double newAmount = rolledFinal - playerBase;
            AttributeModifier rolled = new AttributeModifier(
                original.id(),
                newAmount,
                original.operation()
            );
            rebuilt.add(new ItemAttributeModifiers.Entry(entry.attribute(), rolled, entry.slot()));
        }

        stack.set(DataComponents.ATTRIBUTE_MODIFIERS,
            new ItemAttributeModifiers(rebuilt, current.showInTooltip()));
    }

    private static Double factorFor(Attribute attr, StatsRoller.Factors factors) {
        if (attr == Attributes.ATTACK_DAMAGE.value())    return factors.damage();
        if (attr == Attributes.ATTACK_SPEED.value())     return factors.speed();
        if (attr == Attributes.ARMOR.value())            return factors.armor();
        if (attr == Attributes.ARMOR_TOUGHNESS.value())  return factors.toughness();
        return null;
    }

    private static double playerBaseFor(Attribute attr) {
        if (attr == Attributes.ATTACK_DAMAGE.value()) return PLAYER_BASE_ATTACK_DAMAGE;
        if (attr == Attributes.ATTACK_SPEED.value())  return PLAYER_BASE_ATTACK_SPEED;
        return 0.0;
    }
}
