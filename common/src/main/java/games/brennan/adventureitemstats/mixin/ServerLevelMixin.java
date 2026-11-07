package games.brennan.adventureitemstats.mixin;

import games.brennan.adventureitemstats.internal.RangedDamage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies a bow's or crossbow's Ranged Damage to each arrow as it is launched.
 *
 * <p>{@code addFreshEntity} is the one point every shot passes through with its velocity set, for
 * players and mobs alike, and it is not on the save-load or teleport paths, so an arrow that
 * reloads or crosses a portal can't be boosted twice.</p>
 */
@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Inject(
        method = "addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",
        at = @At("HEAD")
    )
    private void adventureitemstats$applyRangedDamage(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractArrow arrow) {
            RangedDamage.applyToArrow(arrow);
        }
    }
}
