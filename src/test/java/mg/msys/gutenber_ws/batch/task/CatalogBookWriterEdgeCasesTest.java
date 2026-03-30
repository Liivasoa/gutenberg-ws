package mg.msys.gutenber_ws.batch.task;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

@DisplayName("CatalogBookWriter - Edge Cases")
class CatalogBookWriterEdgeCasesTest extends AbstractCatalogBookWriterTestSupport {

    @Test
    @DisplayName("Should handle book with minimal data (only id and title)")
    void shouldHandleMinimalBook() throws Exception {
        GutenbergBook book = new GutenbergBook();
        book.setId(1L);
        book.setTitle("Title");
        Chunk<GutenbergBook> chunk = new Chunk<>(List.of(book));

        catalogBookWriter.write(chunk);
        verify(mockBookWriter).write(chunk);
    }

    @Test
    @DisplayName("Should handle massive batch without errors")
    void shouldHandleMassiveBatch() throws Exception {
        List<GutenbergBook> books = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            books.add(createBook((long) i, "Book " + i, "Author " + i, "en; fr", "Category: Fiction"));
        }
        Chunk<GutenbergBook> chunk = new Chunk<>(books);

        catalogBookWriter.write(chunk);
        verify(mockBookWriter).write(chunk);
    }
}