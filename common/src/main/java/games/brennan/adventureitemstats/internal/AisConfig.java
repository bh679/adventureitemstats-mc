package games.brennan.adventureitemstats.internal;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.slf4j.Logger;

/**
 * AIS's config file: {@code config/adventureitemstats.properties}.
 *
 * <p>A plain {@link Properties} file rather than a loader-native config spec —
 * {@code common/} cannot reference Fabric, Forge, or NeoForge classes, so the
 * config directory is handed in by each loader entrypoint
 * ({@code FMLPaths.CONFIGDIR} / {@code FabricLoader.getConfigDir()}).
 *
 * <p>Immutable: {@link #load} returns a new instance and never mutates an
 * existing one. Parsing is total — a missing, unreadable, or malformed file
 * yields {@link #defaults()} with a logged warning, and a single bad key falls
 * back to that key's default rather than failing the whole file. Config is
 * player-editable data, so bad input must never take the game down.
 */
public final class AisConfig {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String FILE_NAME = "adventureitemstats.properties";

    static final String KEY_RAISE_CAPS = "raiseAttributeCaps";
    static final String KEY_ARMOR_MAX = "armorCapMax";
    static final String KEY_TOUGHNESS_MAX = "armorToughnessCapMax";

    static final boolean DEFAULT_RAISE_CAPS = true;
    static final double DEFAULT_ARMOR_MAX = 1024.0;
    static final double DEFAULT_TOUGHNESS_MAX = 1024.0;

    private static final String FILE_HEADER = """
        Adventure Item Stats configuration.

        raiseAttributeCaps - Vanilla caps generic.armor at 30.0 and generic.armor_toughness
          at 20.0, so rolled gear can promise more armor in its tooltip than the game will
          ever grant (a full armor bar is 20 points, making 30 only 1.5 bars). Set to false
          to restore vanilla clamping. NOTE: this raises the ceiling game-wide, for armor
          from every mod, not only AIS-rolled gear.
        armorCapMax / armorToughnessCapMax - The ceilings to install when the above is true.
          These only ever raise a ceiling; a lower value than the current one is ignored.""";

    private final boolean raiseAttributeCaps;
    private final double armorCapMax;
    private final double armorToughnessCapMax;

    AisConfig(boolean raiseAttributeCaps, double armorCapMax, double armorToughnessCapMax) {
        this.raiseAttributeCaps = raiseAttributeCaps;
        this.armorCapMax = armorCapMax;
        this.armorToughnessCapMax = armorToughnessCapMax;
    }

    public static AisConfig defaults() {
        return new AisConfig(DEFAULT_RAISE_CAPS, DEFAULT_ARMOR_MAX, DEFAULT_TOUGHNESS_MAX);
    }

    public boolean raiseAttributeCaps() {
        return raiseAttributeCaps;
    }

    public double armorCapMax() {
        return armorCapMax;
    }

    public double armorToughnessCapMax() {
        return armorToughnessCapMax;
    }

    /**
     * Reads {@link #FILE_NAME} from {@code configDir}, writing a commented
     * default file first if none exists. Never throws — any I/O or parse
     * failure logs and yields {@link #defaults()}.
     */
    public static AisConfig load(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            if (!Files.exists(file)) {
                writeDefaults(file);
                return defaults();
            }
            Properties properties = new Properties();
            try (InputStream in = Files.newInputStream(file)) {
                properties.load(in);
            }
            return parse(properties);
        } catch (IOException e) {
            LOGGER.warn("[AdventureItemStats] Could not read {} — using defaults", file, e);
            return defaults();
        }
    }

    /** Pure: builds a config from already-loaded properties. Package-visible for unit testing. */
    static AisConfig parse(Properties properties) {
        return new AisConfig(
            parseBoolean(properties.getProperty(KEY_RAISE_CAPS), DEFAULT_RAISE_CAPS, KEY_RAISE_CAPS),
            parsePositiveDouble(properties.getProperty(KEY_ARMOR_MAX), DEFAULT_ARMOR_MAX, KEY_ARMOR_MAX),
            parsePositiveDouble(properties.getProperty(KEY_TOUGHNESS_MAX), DEFAULT_TOUGHNESS_MAX, KEY_TOUGHNESS_MAX)
        );
    }

    private static boolean parseBoolean(String raw, boolean fallback, String key) {
        if (raw == null) {
            return fallback;
        }
        String trimmed = raw.trim();
        if (trimmed.equalsIgnoreCase("true")) {
            return true;
        }
        if (trimmed.equalsIgnoreCase("false")) {
            return false;
        }
        LOGGER.warn("[AdventureItemStats] {}: expected true or false but got '{}' — using {}", key, raw, fallback);
        return fallback;
    }

    private static double parsePositiveDouble(String raw, double fallback, String key) {
        if (raw == null) {
            return fallback;
        }
        try {
            double value = Double.parseDouble(raw.trim());
            if (!Double.isFinite(value) || value <= 0.0) {
                LOGGER.warn("[AdventureItemStats] {}: expected a positive number but got '{}' — using {}", key, raw, fallback);
                return fallback;
            }
            return value;
        } catch (NumberFormatException e) {
            LOGGER.warn("[AdventureItemStats] {}: '{}' is not a number — using {}", key, raw, fallback);
            return fallback;
        }
    }

    private static void writeDefaults(Path file) throws IOException {
        Properties properties = new Properties();
        properties.setProperty(KEY_RAISE_CAPS, String.valueOf(DEFAULT_RAISE_CAPS));
        properties.setProperty(KEY_ARMOR_MAX, String.valueOf(DEFAULT_ARMOR_MAX));
        properties.setProperty(KEY_TOUGHNESS_MAX, String.valueOf(DEFAULT_TOUGHNESS_MAX));
        Files.createDirectories(file.getParent());
        try (OutputStream out = Files.newOutputStream(file)) {
            properties.store(out, FILE_HEADER);
        }
        LOGGER.info("[AdventureItemStats] Wrote default config to {}", file);
    }
}
