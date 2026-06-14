package games.brennan.adventureitemstats.mixin;

import games.brennan.adventureitemstats.internal.RepairStatsResolver;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Preserves a rolled item's stats through crafting-grid repair.
 *
 * <p>Vanilla {@link RepairItemRecipe#assemble} builds a <em>fresh</em>
 * result stack: it carries over only the merged enchantments and the
 * combined durability and never sets {@code ATTRIBUTE_MODIFIERS}, so an
 * item's rolled stats would be lost. This injects at {@code RETURN} and
 * copies the winning input's roll onto the result, leaving vanilla's
 * enchantment merge untouched.
 *
 * <p>The winner is chosen by position — <em>left, then higher</em> (see
 * {@link RepairStatsResolver}). Position is authoritative: a plain item in
 * the winning slot legitimately resets the result to vanilla base stats,
 * and a rolled item in the losing slot is ignored. Only a non-empty
 * modifier set is copied, so base armor — which exposes its protection via
 * {@code Item.getDefaultAttributeModifiers()} and carries an empty
 * {@code ATTRIBUTE_MODIFIERS} — is left at vanilla defaults rather than
 * being zeroed.
 *
 * <p>The anvil path is intentionally untouched: it already copies the
 * left/target item, preserving its roll. Mirrors the cross-loader mixin
 * pattern from {@link LootTableMixin}: lives in {@code common/} and is
 * bundled identically by the Fabric, Forge, and NeoForge jars.
 */
@Mixin(RepairItemRecipe.class)
public abstract class RepairItemRecipeMixin {

    @Inject(
        method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;",
        at = @At("RETURN")
    )
    private void adventureitemstats$preserveRepairStats(
            CraftingInput input, HolderLookup.Provider registries,
            CallbackInfoReturnable<ItemStack> cir) {
        ItemStack result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;

        int width = input.width();
        if (width <= 0) return;

        int size = input.size();
        boolean[] occupied = new boolean[size];
        for (int i = 0; i < size; i++) {
            occupied[i] = !input.getItem(i).isEmpty();
        }

        int winner = RepairStatsResolver.winningIndex(occupied, width);
        if (winner < 0) return;

        ItemAttributeModifiers mods =
            input.getItem(winner).get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (mods != null && !mods.modifiers().isEmpty()) {
            result.set(DataComponents.ATTRIBUTE_MODIFIERS, mods);
        }
    }
}
