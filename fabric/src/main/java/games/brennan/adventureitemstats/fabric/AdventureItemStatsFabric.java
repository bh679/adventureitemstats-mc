package games.brennan.adventureitemstats.fabric;

import com.mojang.logging.LogUtils;
import games.brennan.adventureitemstats.internal.AisBootstrap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

/**
 * Fabric mod entrypoint. Rolling itself still needs no initialisation — the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API. Init exists to load the config and
 * apply the attribute-ceiling raise via {@link AisBootstrap#init}: {@code common/}
 * cannot reference a loader's path API, so the config directory is passed in here.
 */
public final class AdventureItemStatsFabric implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("[AdventureItemStats] Fabric init");
        AisBootstrap.init(FabricLoader.getInstance().getConfigDir());
    }
}
