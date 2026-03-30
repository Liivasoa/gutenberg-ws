package mg.msys.gutenber_ws.batch.task;

import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

@DisplayName("CatalogBookWriter - Smoke Test")
class CatalogBookWriterIntegrationScenariosTest extends AbstractCatalogBookWriterTestSupport {

    @Test
    @DisplayName("Should process a realistic Gutenberg book end-to-end")
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

        catalogBookWriter.write(chunk);
        verify(mockBookWriter).write(chunk);
    }
}