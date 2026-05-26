package games.brennan.adventureitemstats.forge;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

/**
 * Forge mod entrypoint. v0.1 scaffold — initialisation is a no-op; the
 * cross-loader {@link games.brennan.adventureitemstats.mixin.LootTableMixin}
 * routes rolled items through the stats API.
 */
@Mod("adventureitemstats")
public final class AdventureItemStatsForge {

    private static final Logger LOGGER = LogUtils.getLogger();

    public AdventureItemStatsForge(IEventBus modBus) {
        LOGGER.info("[AdventureItemStats] Forge init");
    }
}
