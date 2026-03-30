package mg.msys.gutenber_ws.batch.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import mg.msys.gutenber_ws.batch.entity.Author;

@DisplayName("CatalogBookMetadataParser - Author Parsing")
class CatalogBookMetadataParserAuthorParsingTest {

    private final CatalogBookMetadataParser parser = new CatalogBookMetadataParser();

    @Test
    @DisplayName("Should parse authors with years and normalized keys")
    void shouldParseAuthorsWithYearsAndNormalizedKeys() {
        List<Author> authors = parser.parseAuthors("Shakespeare, William, 1564 - 1616; Müller, François");

        assertEquals(
                List.of(
                        new Author("Shakespeare", "William", 1564, 1616, "shakespeare|william|1564|1616"),
                        new Author("Müller", "François", null, null, "muller|francois||")),
                authors);
    }

    @Test
    @DisplayName("Should keep death year null for open ended ranges")
    void shouldKeepDeathYearNullForOpenEndedRanges() {
        List<Author> authors = parser.parseAuthors("Anonymous, 1900 - ");

        assertEquals(List.of(new Author("Anonymous", null, 1900, null, "anonymous||1900|")), authors);
    }

    @Test
    @DisplayName("Should ignore null blank and empty author tokens")
    void shouldIgnoreNullBlankAndEmptyAuthorTokens() {
        assertTrue(parser.parseAuthors(null).isEmpty());
        assertTrue(parser.parseAuthors("   ").isEmpty());
        assertEquals(List.of(new Author("Smith", "John", null, null, "smith|john||")),
                parser.parseAuthors(" ; Smith, John ; "));
    }
}