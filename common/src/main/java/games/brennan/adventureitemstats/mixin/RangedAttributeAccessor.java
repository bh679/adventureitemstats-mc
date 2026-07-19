package games.brennan.adventureitemstats.mixin;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Write access to {@link RangedAttribute}'s otherwise-final {@code maxValue}.
 *
 * <p>Vanilla ships {@code generic.armor} with a ceiling of {@code 30.0} and
 * {@code generic.armor_toughness} with {@code 20.0}, and
 * {@code AttributeInstance.getValue()} clamps to that range on every read. A
 * rolled item can therefore advertise more armor in its tooltip than the game
 * will ever grant — AIS adds its progression bonus
 * {@linkplain games.brennan.adventureitemstats.api.StatsModifier#finalAmount
 * un-clamped} by design, so past 30 the surplus is silently discarded. Weapons
 * are unaffected: attack damage caps at {@code 2048.0}, attack speed at
 * {@code 1024.0}.
 *
 * <p>{@code maxValue} is {@code private final}, hence {@link Mutable} alongside
 * the {@link Accessor}. Applied once at start-up by
 * {@link games.brennan.adventureitemstats.internal.AttributeCaps}; nothing else
 * should touch it.
 *
 * <p>Lives in {@code common/} and is bundled identically by the Fabric, Forge,
 * and NeoForge jars, matching the cross-loader mixin pattern used by
 * {@link LootTableMixin} and {@link RepairItemRecipeMixin}.
 */
@Mixin(RangedAttribute.class)
public interface RangedAttributeAccessor {

    @Mutable
    @Accessor("maxValue")
    void adventureitemstats$setMaxValue(double maxValue);

    @Accessor("maxValue")
    double adventureitemstats$getMaxValue();
}
