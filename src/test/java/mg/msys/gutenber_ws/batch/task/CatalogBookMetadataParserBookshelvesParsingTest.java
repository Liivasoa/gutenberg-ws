package mg.msys.gutenber_ws.batch.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CatalogBookMetadataParser - Bookshelves Parsing")
class CatalogBookMetadataParserBookshelvesParsingTest {

    private final CatalogBookMetadataParser parser = new CatalogBookMetadataParser();

    @Test
    @DisplayName("Should classify category and topic facets from bookshelf values")
    void shouldClassifyCategoryAndTopicFacetsFromBookshelfValues() {
        List<CatalogBookMetadataParser.FacetEntry> facets = parser.parseBookshelves(
                "Category: Science Fiction; History; category : Études  Littéraires");

        assertEquals(
                List.of(
                        new CatalogBookMetadataParser.FacetEntry("category", "Science Fiction",
                                "science fiction"),
                        new CatalogBookMetadataParser.FacetEntry("topic", "History", "history"),
                        new CatalogBookMetadataParser.FacetEntry("category", "Études  Littéraires",
                                "etudes litteraires")),
                facets);
    }

    @Test
    @DisplayName("Should keep non category prefixes as topic values")
    void shouldKeepNonCategoryPrefixesAsTopicValues() {
        List<CatalogBookMetadataParser.FacetEntry> facets = parser.parseBookshelves("Genre: Adventure");

        assertEquals(List.of(new CatalogBookMetadataParser.FacetEntry("topic", "Genre: Adventure",
                "genre: adventure")), facets);
    }

    @Test
    @DisplayName("Should ignore null blank and empty bookshelf tokens")
    void shouldIgnoreNullBlankAndEmptyBookshelfTokens() {
        assertTrue(parser.parseBookshelves(null).isEmpty());
        assertTrue(parser.parseBookshelves("   ").isEmpty());
        assertEquals(List.of(new CatalogBookMetadataParser.FacetEntry("topic", "Poetry", "poetry")),
                parser.parseBookshelves(" ; Category: ; Poetry ; "));
    }
}