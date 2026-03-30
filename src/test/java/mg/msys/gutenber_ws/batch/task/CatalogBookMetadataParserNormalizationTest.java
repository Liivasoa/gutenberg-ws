package mg.msys.gutenber_ws.batch.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CatalogBookMetadataParser - Normalization")
class CatalogBookMetadataParserNormalizationTest {

    private final CatalogBookMetadataParser parser = new CatalogBookMetadataParser();

    @Test
    @DisplayName("Should normalize accents case and repeated whitespace")
    void shouldNormalizeAccentsCaseAndRepeatedWhitespace() {
        assertEquals("litterature francaise", parser.normalize("  Littérature   Française  "));
    }

    @Test
    @DisplayName("Should normalize null to empty string")
    void shouldNormalizeNullToEmptyString() {
        assertEquals("", parser.normalize(null));
    }

    @Test
    @DisplayName("Should trim and filter language codes")
    void shouldTrimAndFilterLanguageCodes() {
        assertEquals(List.of("en", "fr", "de"), parser.parseLanguageCodes(" en ; fr ; ; de ; "));
        assertTrue(parser.parseLanguageCodes("   ").isEmpty());
        assertTrue(parser.parseLanguageCodes(null).isEmpty());
    }

    @Test
    @DisplayName("Should parse numeric years and reject invalid values")
    void shouldParseNumericYearsAndRejectInvalidValues() {
        assertEquals(42, parser.parseYear(" 0042 "));
        assertNull(parser.parseYear(null));
        assertNull(parser.parseYear("   "));
        assertNull(parser.parseYear("19A0"));
        assertNull(parser.parseYear("19000"));
    }
}