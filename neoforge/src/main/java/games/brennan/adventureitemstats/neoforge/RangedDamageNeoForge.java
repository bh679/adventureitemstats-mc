package games.brennan.adventureitemstats.neoforge;

import games.brennan.adventureitemstats.internal.RangedDamage;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * NeoForge side of the Ranged Damage stat: registers it, and makes it read as an absolute number,
 * the way a sword's Attack Damage does.
 */
final class RangedDamageNeoForge {

    private static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, RangedDamage.ID.getNamespace());

    private static final DeferredHolder<Attribute, Attribute> RANGED_DAMAGE =
        ATTRIBUTES.register(RangedDamage.ID.getPath(), BaseLineAttribute::new);

    private RangedDamageNeoForge() {}

    static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
        modBus.addListener(RangedDamageNeoForge::addToPlayers);
        modBus.addListener(RangedDamageNeoForge::addDefaultStat);
    }

    /**
     * A base modifier makes NeoForge's tooltip ask the viewing player for the attribute's base
     * value, and that lookup throws for an attribute the player doesn't have.
     */
    private static void addToPlayers(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, RANGED_DAMAGE);
    }

    /** Every bow and crossbow shows its stat, crafted and already-owned ones included. */
    private static void addDefaultStat(ModifyDefaultComponentsEvent event) {
        Holder<Attribute> attribute = RANGED_DAMAGE.getDelegate();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!RangedDamage.isRangedWeapon(item)) continue;
            ItemAttributeModifiers existing = item.components()
                .getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
            event.modify(item, patch -> patch.set(DataComponents.ATTRIBUTE_MODIFIERS,
                RangedDamage.withBase(existing, attribute, item)));
        }
    }

    /** {@code getBaseId} is what turns a {@code "+6"} line into an absolute {@code "6"}. */
    private static final class BaseLineAttribute extends RangedAttribute {
        BaseLineAttribute() {
            super(RangedDamage.DESCRIPTION_KEY, 0.0, 0.0, RangedDamage.MAX_VALUE);
        }

        @Override
        public ResourceLocation getBaseId() {
            return RangedDamage.BASE_MODIFIER_ID;
        }
    }
}
