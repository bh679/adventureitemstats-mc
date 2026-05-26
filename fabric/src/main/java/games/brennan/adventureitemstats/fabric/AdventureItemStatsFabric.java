package games.brennan.adventureitemstats.fabric;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

/**
 * Fabric mod entrypoint. v0.1 scaffold — initialisation is a no-op; the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API.
 */
public final class AdventureItemStatsFabric implements ModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        LOGGER.info("[AdventureItemStats] Fabric init");
    }
}
