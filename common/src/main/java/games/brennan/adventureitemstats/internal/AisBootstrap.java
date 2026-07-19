package games.brennan.adventureitemstats.internal;

import java.nio.file.Path;

/**
 * The one start-up routine shared by the Fabric, Forge, and NeoForge
 * entrypoints, so loader-specific classes stay confined to those three files.
 *
 * <p>{@code configDir} is supplied by the caller because {@code common/} cannot
 * reference a loader's path API.
 */
public final class AisBootstrap {

    private AisBootstrap() {}

    /** Loads the config and applies whatever it enables. Safe to call more than once. */
    public static void init(Path configDir) {
        AisConfig config = AisConfig.load(configDir);
        if (config.raiseAttributeCaps()) {
            AttributeCaps.apply(config.armorCapMax(), config.armorToughnessCapMax());
        }
    }
}
