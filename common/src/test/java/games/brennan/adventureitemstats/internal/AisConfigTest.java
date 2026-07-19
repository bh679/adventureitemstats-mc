package games.brennan.adventureitemstats.internal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link AisConfig}. Config is player-editable data, so the contract
 * under test is that malformed input degrades to the default rather than
 * throwing — a bad key must not take the game down, and must not poison the
 * keys around it.
 */
class AisConfigTest {

    private static final double EPS = 1.0e-9;

    private static Properties props(String... keyValuePairs) {
        Properties p = new Properties();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            p.setProperty(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return p;
    }

    @Test
    void emptyPropertiesYieldDefaults() {
        AisConfig config = AisConfig.parse(new Properties());
        assertTrue(config.raiseAttributeCaps());
        assertEquals(AisConfig.DEFAULT_ARMOR_MAX, config.armorCapMax(), EPS);
        assertEquals(AisConfig.DEFAULT_TOUGHNESS_MAX, config.armorToughnessCapMax(), EPS);
    }

    @Test
    void readsExplicitValues() {
        AisConfig config = AisConfig.parse(props(
            AisConfig.KEY_RAISE_CAPS, "false",
            AisConfig.KEY_ARMOR_MAX, "64.0",
            AisConfig.KEY_TOUGHNESS_MAX, "48.5"));
        assertFalse(config.raiseAttributeCaps());
        assertEquals(64.0, config.armorCapMax(), EPS);
        assertEquals(48.5, config.armorToughnessCapMax(), EPS);
    }

    @Test
    void booleanParsingIsCaseInsensitiveAndTrimmed() {
        assertFalse(AisConfig.parse(props(AisConfig.KEY_RAISE_CAPS, "  FALSE ")).raiseAttributeCaps());
        assertTrue(AisConfig.parse(props(AisConfig.KEY_RAISE_CAPS, "True")).raiseAttributeCaps());
    }

    /** A typo must not silently disable the feature — it falls back to the default. */
    @Test
    void unparseableBooleanFallsBackToDefault() {
        assertTrue(AisConfig.parse(props(AisConfig.KEY_RAISE_CAPS, "yes-please")).raiseAttributeCaps());
    }

    @Test
    void rejectsNonNumericAndNonPositiveCaps() {
        for (String bad : new String[] {"not-a-number", "0", "-5", "NaN"}) {
            AisConfig config = AisConfig.parse(props(AisConfig.KEY_ARMOR_MAX, bad));
            assertEquals(AisConfig.DEFAULT_ARMOR_MAX, config.armorCapMax(), EPS, "input: " + bad);
        }
    }

    /** One bad key must not drag the others down with it. */
    @Test
    void oneBadKeyDoesNotAffectOthers() {
        AisConfig config = AisConfig.parse(props(
            AisConfig.KEY_ARMOR_MAX, "garbage",
            AisConfig.KEY_TOUGHNESS_MAX, "77.0"));
        assertEquals(AisConfig.DEFAULT_ARMOR_MAX, config.armorCapMax(), EPS);
        assertEquals(77.0, config.armorToughnessCapMax(), EPS);
    }

    @Test
    void loadWritesDefaultFileWhenMissing(@TempDir Path dir) {
        AisConfig config = AisConfig.load(dir);
        assertTrue(Files.exists(dir.resolve(AisConfig.FILE_NAME)), "default config should be written");
        assertTrue(config.raiseAttributeCaps());
    }

    @Test
    void loadRoundTripsAWrittenFile(@TempDir Path dir) throws IOException {
        Files.writeString(dir.resolve(AisConfig.FILE_NAME),
            AisConfig.KEY_RAISE_CAPS + "=false\n" + AisConfig.KEY_ARMOR_MAX + "=99.0\n");
        AisConfig config = AisConfig.load(dir);
        assertFalse(config.raiseAttributeCaps());
        assertEquals(99.0, config.armorCapMax(), EPS);
    }

    /** A directory where the file should be is unreadable as a file — must not throw. */
    @Test
    void loadFallsBackToDefaultsOnIoFailure(@TempDir Path dir) throws IOException {
        Files.createDirectory(dir.resolve(AisConfig.FILE_NAME));
        AisConfig config = AisConfig.load(dir);
        assertTrue(config.raiseAttributeCaps());
        assertEquals(AisConfig.DEFAULT_ARMOR_MAX, config.armorCapMax(), EPS);
    }
}
