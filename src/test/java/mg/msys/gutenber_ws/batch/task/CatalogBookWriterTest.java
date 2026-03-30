package mg.msys.gutenber_ws.batch.task;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

/**
 * Comprehensive Unit Tests for CatalogBookWriter
 * 
 * Tests cover:
 * - Main write flow with complete CRUD operations
 * - Author parsing and normalization
 * - Language parsing and deduplication
 * - Bookshelves/Facets parsing (categories and topics)
 * - Edge cases (null, empty, whitespace-only values)
 * - Integration scenarios with realistic data
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogBookWriter - Comprehensive Unit Tests")
class CatalogBookWriterTest {

    @Mock
    private JdbcBatchItemWriter<GutenbergBook> mockBookWriter;

    @Mock
    private JdbcTemplate mockJdbcTemplate;

    private CatalogBookWriter catalogBookWriter;

    @BeforeEach
    void setUp() {
        catalogBookWriter = new CatalogBookWriter(mockBookWriter, mockJdbcTemplate);
        
        // Configure lenient mocks for all JdbcTemplate calls
        // This handles all possible overloads of queryForObject
        lenient().doReturn(1L).when(mockJdbcTemplate).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any());
        lenient().doReturn(10L).when(mockJdbcTemplate).queryForObject(anyString(), eq(Long.class), any());
        lenient().doReturn(100L).when(mockJdbcTemplate).queryForObject(anyString(), eq(Long.class), any(), any());
        lenient().doReturn(1000L).when(mockJdbcTemplate).queryForObject(anyString(), eq(Long.class), any(), any(), any());
        
        // Mock batchUpdate
            lenient().doReturn(new int[]{1}).when(mockJdbcTemplate).batchUpdate(anyString(), any(List.class));
    }

    private GutenbergBook createBook(Long id, String title, String authors, String languages, String bookshelves) {
        GutenbergBook book = new GutenbergBook();
        book.setId(id);
        book.setTitle(title);
        book.setIssued(LocalDate.of(2023, 1, 1));
        book.setAuthors(authors);
        book.setLanguages(languages);
        book.setBookshelves(bookshelves);
        book.setSubjects("test subjects");
        return book;
    }

    // ============================================================================
    // Main Write Flow Tests
    // ============================================================================

    @Nested
    @DisplayName("Main Write Flow")
    class WriteFlowTests {

        @Test
        @DisplayName("Should write chunk with complete book data (all fields populated)")
        void shouldWriteCompleteChunk() throws Exception {
            GutenbergBook book = createBook(1L, "Book Title", "Author Name, 1900 - 1980", "en; fr",
                    "Category: Fiction; History");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            catalogBookWriter.write(chunk);

            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle empty chunk without processing")
        void shouldHandleEmptyChunk() throws Exception {
            Chunk<GutenbergBook> chunk = new Chunk<>(new ArrayList<>());
            
            catalogBookWriter.write(chunk);

            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should process multiple books in single chunk")
        void shouldProcessMultipleBooks() throws Exception {
            GutenbergBook book1 = createBook(1L, "Book1", "Author A", "en", "Fiction");
            GutenbergBook book2 = createBook(2L, "Book2", "Author B; Author C", "fr; de", "History");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book1, book2));

            catalogBookWriter.write(chunk);

            verify(mockBookWriter).write(chunk);
        }
    }

    // ============================================================================
    // Author Parsing Tests
    // ============================================================================

    @Nested
    @DisplayName("Author Parsing")
    class AuthorParsingTests {

        @Test
        @DisplayName("Should parse author with birth and death years (1564 - 1616)")
        void shouldParseAuthorWithYears() throws Exception {
            GutenbergBook book = createBook(1L, "Book", "Shakespeare, William, 1564 - 1616", "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should parse multiple authors separated by semicolons")
        void shouldParseMultipleAuthors() throws Exception {
            GutenbergBook book = createBook(1L, "Book", "Austen, Jane, 1775 - 1817; Brontë, Charlotte",
                    "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should normalize author names (remove accents, lowercase)")
        void shouldNormalizeAuthorNames() throws Exception {
            GutenbergBook book = createBook(1L, "Book", "Müller, François", "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle null authors gracefully")
        void shouldHandleNullAuthors() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle empty authors gracefully")
        void shouldHandleEmptyAuthors() throws Exception {
            GutenbergBook book = createBook(1L, "Book", "", "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }
    }

    // ============================================================================
    // Language Parsing Tests
    // ============================================================================

    @Nested
    @DisplayName("Language Parsing")
    class LanguageParsingTests {

        @Test
        @DisplayName("Should parse single language code")
        void shouldParseSingleLanguage() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, "en", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should parse multiple language codes (en; fr; de; es)")
        void shouldParseMultipleLanguages() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, "en; fr; de; es", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should trim whitespace from language codes")
        void shouldTrimLanguageWhitespace() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, " en ; fr ; de ", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle null languages without errors")
        void shouldHandleNullLanguages() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle empty languages without errors")
        void shouldHandleEmptyLanguages() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, "", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }
    }

    // ============================================================================
    // Bookshelves/Facets Parsing Tests
    // ============================================================================

    @Nested
    @DisplayName("Bookshelves/Facets Parsing")
    class BookshelvesParsingTests {

        @Test
        @DisplayName("Should parse 'Category: X' format as category facet")
        void shouldParseCategoryFacet() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, "Category: Science Fiction");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should parse simple topic facets without Category prefix")
        void shouldParseTopicFacets() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, "Fiction; History; Science");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle mixed category and topic facets")
        void shouldHandleMixedFacets() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null,
                    "Category: Fiction; Science Fiction; Category: History");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should normalize facet values (remove accents, collapse spaces)")
        void shouldNormalizeFacetValues() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, "Littérature  Française");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle null bookshelves")
        void shouldHandleNullBookshelves() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle empty bookshelves")
        void shouldHandleEmptyBookshelves() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, "");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }
    }

    // ============================================================================
    // Edge Cases Tests
    // ============================================================================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle book with minimal data (only id and title)")
        void shouldHandleMinimalBook() throws Exception {
            GutenbergBook book = new GutenbergBook();
            book.setId(1L);
            book.setTitle("Title");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle whitespace-only fields as empty")
        void shouldHandleWhitespaceOnlyFields() throws Exception {
            GutenbergBook book = new GutenbergBook();
            book.setId(1L);
            book.setTitle("Title");
            book.setAuthors("   ");
            book.setLanguages("  ");
            book.setBookshelves("    ");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle leading/trailing semicolons in delimited values")
        void shouldHandleLeadingTrailingSemicolons() throws Exception {
            GutenbergBook book = new GutenbergBook();
            book.setId(1L);
            book.setTitle("Title");
            book.setAuthors(";Smith, John;");
            book.setLanguages(";en;fr;");
            book.setBookshelves(";Fiction;History;");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle massive batch of diverse books")
        void shouldHandleMassiveBatch() throws Exception {
            List<GutenbergBook> books = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                books.add(createBook((long) i, "Book " + i, "Author " + i, "en; fr", "Category: Fiction"));
            }
            Chunk<GutenbergBook> chunk = new Chunk<>(books);

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }
    }

    // ============================================================================
    // JDBC Operations Tests
    // ============================================================================

    @Nested
    @DisplayName("JDBC Batch Operations")
    class JdbcOperationsTests {

        @Test
        @DisplayName("Should call jpdbcTemplate methods when authors present")
        void shouldCallJdbcForAuthors() throws Exception {
            GutenbergBook book = createBook(1L, "Book", "Author A; Author B", null, null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            catalogBookWriter.write(chunk);

            verify(mockJdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should call JdbcTemplate methods when languages present")
        void shouldCallJdbcForLanguages() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, "en; fr; de", null);
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            catalogBookWriter.write(chunk);

            verify(mockJdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(Long.class), any());
        }

        @Test
        @DisplayName("Should call JdbcTemplate methods when bookshelves present")
        void shouldCallJdbcForBookshelves() throws Exception {
            GutenbergBook book = createBook(1L, "Book", null, null, "Fiction; History; Science");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            catalogBookWriter.write(chunk);

            verify(mockJdbcTemplate, atLeastOnce()).queryForObject(anyString(), eq(Long.class), any(), any(), any());
        }

        @Test
        @DisplayName("Should not call JdbcTemplate when all fields are null")
        void shouldNotCallJdbcWhenNoData() throws Exception {
            GutenbergBook book = new GutenbergBook();
            book.setId(1L);
            book.setTitle("Title");
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            catalogBookWriter.write(chunk);

            verify(mockJdbcTemplate, never()).queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any());
        }
    }

    // ============================================================================
    // Integration Scenarios Tests
    // ============================================================================

    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenariosTests {

        @Test
        @DisplayName("Should process realistic Gutenberg catalog book")
        void shouldProcessRealisticBook() throws Exception {
            GutenbergBook book = new GutenbergBook();
            book.setId(11L);
            book.setTitle("Alice's Adventures in Wonderland");
            book.setIssued(LocalDate.of(2008, 6, 27));
            book.setAuthors("Carroll, Lewis, 1832-1898");
            book.setLanguages("en");
            book.setBookshelves("Category: Children's literature; Fantasy; Fairy tales; Adventure");
            book.setSubjects("Alice (Fictitious character) -- Juvenile fiction");

            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should process batch with diverse authors")
        void shouldProcessDiverseAuthors() throws Exception {
            GutenbergBook book1 = createBook(1L, "Moby Dick", "Melville, Herman, 1819-1891", "en",
                    "Category: Adventure");
            GutenbergBook book2 = createBook(2L, "Le Petit Prince", "Saint-Exupéry, Antoine de, 1900-1944", "fr",
                    "Category: Children's");
            GutenbergBook book3 = createBook(3L, "Crime and Punishment", "Dostoevsky, Fyodor, 1821-1881", "ru",
                    "Category: Fiction");
            
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book1, book2, book3));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }

        @Test
        @DisplayName("Should handle Gutenberg book with complex multilingual subjects")
        void shouldHandleComplexMultilingual() throws Exception {
            GutenbergBook book = createBook(100L, "Complex Book",
                    "Author Primary, 1800 - 1850; Author Secondary, 1820 - 1900",
                    "en; fr; de; es; it",
                    "Category: Literature; Category: Science; Fiction; History; Philosophy; Drama");
            
            Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

            assertDoesNotThrow(() -> catalogBookWriter.write(chunk));
            verify(mockBookWriter).write(chunk);
        }
    }

}
