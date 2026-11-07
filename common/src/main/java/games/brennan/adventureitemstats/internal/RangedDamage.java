package games.brennan.adventureitemstats.internal;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The Ranged Damage stat: what a full-power shot from a bow or crossbow deals, stored as an
 * attribute modifier so tooltips, anvils and repair treat it like any other rolled stat.
 *
 * <p>The amount is absolute, the same way a sword's Attack Damage reads. Vanilla computes arrow
 * damage at hit time as {@code launch speed * base damage}, so a plain bow's full draw (3.0 * 2.0)
 * deals {@link #NOMINAL_BOW} and a crossbow's (3.15 * 2.0, rounded up) deals
 * {@link #NOMINAL_CROSSBOW}. A shot gains the difference between its weapon's stat and that
 * nominal; a plain weapon gains nothing, so vanilla damage is untouched.</p>
 *
 * <p>Only the weapon's own modifier is ever read. Entities need no instance of the attribute to
 * shoot, and vanilla skips attributes an entity lacks when it equips a stack.</p>
 */
public final class RangedDamage {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(StatsRoller.NAMESPACE, "ranged_damage");
    public static final ResourceLocation BASE_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(StatsRoller.NAMESPACE, "base_ranged_damage");
    public static final String DESCRIPTION_KEY = "attribute.name." + StatsRoller.NAMESPACE + ".ranged_damage";

    /** Attribute value ceiling, matching the raised vanilla ceilings in {@link AttributeCaps}. */
    public static final double MAX_VALUE = 1024.0;

    public static final double NOMINAL_BOW = 6.0;
    public static final double NOMINAL_CROSSBOW = 7.0;

    /** A player bow's launch speed at full draw. A weaker draw flies slower, so it earns less of the gain. */
    static final double FULL_DRAW_SPEED = 3.0;

    private static final ResourceKey<Attribute> KEY = ResourceKey.create(Registries.ATTRIBUTE, ID);

    private RangedDamage() {}

    /** The plain attribute, for loaders with no notion of a base modifier. */
    public static Attribute createAttribute() {
        return new RangedAttribute(DESCRIPTION_KEY, 0.0, 0.0, MAX_VALUE);
    }

    /** The registered attribute, or empty before registration. */
    public static Optional<? extends Holder<Attribute>> holder() {
        return BuiltInRegistries.ATTRIBUTE.getHolder(KEY);
    }

    public static boolean isRangedWeapon(Item item) {
        return item instanceof ProjectileWeaponItem;
    }

    static double nominal(boolean crossbow) {
        return crossbow ? NOMINAL_CROSSBOW : NOMINAL_BOW;
    }

    /** What a plain copy of {@code item} deals on a full-power shot. */
    public static double nominalFor(Item item) {
        return nominal(item instanceof CrossbowItem);
    }

    /** The base-damage change a shot needs so that it deals {@code stat} at the reference speed. Pure. */
    static double baseDamageGain(double stat, double nominal, double referenceSpeed) {
        return (stat - nominal) / referenceSpeed;
    }

    /** Base damage after the gain; a poorly rolled weapon can lower it but never below zero. Pure. */
    static double boostedBaseDamage(double baseDamage, double gain) {
        return Math.max(0.0, baseDamage + gain);
    }

    /** The modifier entry a plain {@code item} carries. */
    public static ItemAttributeModifiers.Entry baseEntry(Holder<Attribute> attribute, Item item) {
        AttributeModifier modifier = new AttributeModifier(
            BASE_MODIFIER_ID, nominalFor(item), AttributeModifier.Operation.ADD_VALUE);
        return new ItemAttributeModifiers.Entry(attribute, modifier, EquipmentSlotGroup.HAND);
    }

    /** {@code current} with {@code item}'s base entry added, if it holds no Ranged Damage yet. */
    public static ItemAttributeModifiers withBase(ItemAttributeModifiers current, Holder<Attribute> attribute, Item item) {
        for (ItemAttributeModifiers.Entry entry : current.modifiers()) {
            if (entry.attribute().value() == attribute.value()) return current;
        }
        List<ItemAttributeModifiers.Entry> entries = new ArrayList<>(current.modifiers());
        entries.add(baseEntry(attribute, item));
        return new ItemAttributeModifiers(entries, current.showInTooltip());
    }

    /** The Ranged Damage {@code stack} carries, or 0 when it has none. */
    public static double of(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0.0;
        Optional<? extends Holder<Attribute>> attribute = holder();
        if (attribute.isEmpty()) return 0.0;
        ItemAttributeModifiers modifiers = stack.getOrDefault(
            DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double total = 0.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().value() == attribute.get().value()
                    && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                total += entry.modifier().amount();
            }
        }
        return total;
    }

    /**
     * Applies the firing weapon's stat to a freshly launched arrow.
     *
     * <p>A player's bow references the full-draw speed, so a half-drawn shot keeps vanilla's
     * proportional damage. Crossbows always fire at full power, and mobs shoot slower than any
     * player does, so both reference their real launch speed and land the whole gain.</p>
     */
    public static void applyToArrow(AbstractArrow arrow) {
        ItemStack weapon = arrow.getWeaponItem();
        if (weapon == null || !isRangedWeapon(weapon.getItem())) return;
        double stat = of(weapon);
        if (stat <= 0.0) return;

        double referenceSpeed = arrow.getOwner() instanceof Player && weapon.getItem() instanceof BowItem
            ? FULL_DRAW_SPEED
            : arrow.getDeltaMovement().length();
        if (referenceSpeed < 0.01) return;

        double gain = baseDamageGain(stat, nominalFor(weapon.getItem()), referenceSpeed);
        arrow.setBaseDamage(boostedBaseDamage(arrow.getBaseDamage(), gain));
    }
}
