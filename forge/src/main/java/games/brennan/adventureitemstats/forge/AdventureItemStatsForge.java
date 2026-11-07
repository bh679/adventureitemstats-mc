package games.brennan.adventureitemstats.forge;

import com.mojang.logging.LogUtils;
import games.brennan.adventureitemstats.internal.AisBootstrap;
import games.brennan.adventureitemstats.internal.RangedDamage;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import org.slf4j.Logger;

/**
 * Forge mod entrypoint. Rolling itself still needs no initialisation — the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API. Init exists to load the config and
 * apply the attribute-ceiling raise via {@link AisBootstrap#init}: {@code common/}
 * cannot reference a loader's path API, so the config directory is passed in here.
 */
@Mod("adventureitemstats")
public final class AdventureItemStatsForge {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final DeferredRegister<Attribute> ATTRIBUTES =
        DeferredRegister.create(Registries.ATTRIBUTE, RangedDamage.ID.getNamespace());

    public AdventureItemStatsForge(IEventBus modBus) {
        LOGGER.info("[AdventureItemStats] Forge init");
        AisBootstrap.init(FMLPaths.CONFIGDIR.get());
        ATTRIBUTES.register(RangedDamage.ID.getPath(), RangedDamage::createAttribute);
        ATTRIBUTES.register(modBus);
    }
}
