package games.brennan.adventureitemstats.neoforge;

import com.mojang.logging.LogUtils;
import games.brennan.adventureitemstats.internal.AisBootstrap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

/**
 * NeoForge mod entrypoint. Rolling itself still needs no initialisation — the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API. Init exists to load the config and
 * apply the attribute-ceiling raise via {@link AisBootstrap#init}: {@code common/}
 * cannot reference a loader's path API, so the config directory is passed in here.
 */
@Mod("adventureitemstats")
public final class AdventureItemStatsNeoForge {

    private static final Logger LOGGER = LogUtils.getLogger();

    public AdventureItemStatsNeoForge(IEventBus modBus) {
        LOGGER.info("[AdventureItemStats] NeoForge init");
        AisBootstrap.init(FMLPaths.CONFIGDIR.get());
    }
}
