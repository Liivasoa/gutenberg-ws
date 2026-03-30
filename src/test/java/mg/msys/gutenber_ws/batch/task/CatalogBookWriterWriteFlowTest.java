package mg.msys.gutenber_ws.batch.task;

import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.Chunk;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

@DisplayName("CatalogBookWriter - Write Flow")
class CatalogBookWriterWriteFlowTest extends AbstractCatalogBookWriterTestSupport {

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