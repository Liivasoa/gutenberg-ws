package mg.msys.gutenber_ws.batch.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

@ExtendWith(MockitoExtension.class)
abstract class AbstractCatalogBookWriterTestSupport {

    @Mock
    protected JdbcBatchItemWriter<GutenbergBook> mockBookWriter;

    @Mock
    protected JdbcTemplate mockJdbcTemplate;

    protected CatalogBookWriter catalogBookWriter;

    @BeforeEach
    void setUp() {
        catalogBookWriter = new CatalogBookWriter(mockBookWriter, mockJdbcTemplate);

        lenient().doReturn(1L).when(mockJdbcTemplate)
                .queryForObject(anyString(), eq(Long.class), any(), any(), any(), any(), any());
        lenient().doReturn(10L).when(mockJdbcTemplate)
                .queryForObject(anyString(), eq(Long.class), any());
        lenient().doReturn(100L).when(mockJdbcTemplate)
                .queryForObject(anyString(), eq(Long.class), any(), any());
        lenient().doReturn(1000L).when(mockJdbcTemplate)
                .queryForObject(anyString(), eq(Long.class), any(), any(), any());
        lenient().doReturn(new int[] { 1 }).when(mockJdbcTemplate).batchUpdate(anyString(), any(List.class));
    }

    protected GutenbergBook createBook(Long id, String title, String authors, String languages, String bookshelves) {
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
}