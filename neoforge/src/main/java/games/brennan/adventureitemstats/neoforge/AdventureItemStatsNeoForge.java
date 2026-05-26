package games.brennan.adventureitemstats.neoforge;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

/**
 * NeoForge mod entrypoint. v0.1 scaffold — initialisation is a no-op; the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API.
 */
@Mod("adventureitemstats")
public final class AdventureItemStatsNeoForge {

    private static final Logger LOGGER = LogUtils.getLogger();

    public AdventureItemStatsNeoForge(IEventBus modBus) {
        LOGGER.info("[AdventureItemStats] NeoForge init");
    }
}
