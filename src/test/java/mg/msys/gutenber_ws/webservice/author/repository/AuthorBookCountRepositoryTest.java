package mg.msys.gutenber_ws.webservice.author.repository;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import mg.msys.gutenber_ws.webservice.shared.repository.AbstractRepositoryTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AuthorBookCountRepository - Pagination and Sorting")
class AuthorBookCountRepositoryTest extends AbstractRepositoryTest {

    private AuthorBookCountRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setUp() {
        repository = new AuthorBookCountJdbcRepository(jdbcTemplate);
    }

    @BeforeAll
    void seedAndRefresh() {
        jdbcTemplate.update("DELETE FROM book_author");
        jdbcTemplate.update("DELETE FROM book");
        jdbcTemplate.update("DELETE FROM author");

        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (100, 'B1') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (101, 'B2') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (102, 'B3') ON CONFLICT DO NOTHING");

        jdbcTemplate
                .update("""
                        INSERT INTO author (last_name, first_names, normalized_key) VALUES ('Wilde', 'Oscar', 'wilde_oscar') ON CONFLICT DO NOTHING
                        """);
        jdbcTemplate
                .update("""
                        INSERT INTO author (last_name, first_names, normalized_key) VALUES ('Austen', 'Jane', 'austen_jane') ON CONFLICT DO NOTHING
                        """);
        jdbcTemplate
                .update("""
                        INSERT INTO author (last_name, first_names, normalized_key) VALUES ('Dickens', 'Charles', 'dickens_charles') ON CONFLICT DO NOTHING
                        """);

        // Wilde has 2 books, Austen has 1, Dickens has 0
        jdbcTemplate.update("""
                INSERT INTO book_author (book_id, author_id)
                SELECT 100, id FROM author WHERE normalized_key = 'wilde_oscar' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_author (book_id, author_id)
                SELECT 101, id FROM author WHERE normalized_key = 'wilde_oscar' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_author (book_id, author_id)
                SELECT 102, id FROM author WHERE normalized_key = 'austen_jane' ON CONFLICT DO NOTHING
                """);

        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW author_book_count_mv");
    }

    @Test
    @DisplayName("Should return paginated authors with correct totals")
    void shouldReturnPaginatedAuthorsWithCorrectTotals() {
        Page<AuthorBookCountDto> result = repository.findAll(PageRequest.of(0, 10));
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should sort authors by lastName ascending by default")
    void shouldSortByLastNameAscending() {
        Page<AuthorBookCountDto> result = repository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "lastName")));
        List<String> names = result.getContent().stream().map(AuthorBookCountDto::lastName).toList();
        assertThat(names).containsExactly("Austen", "Dickens", "Wilde");
    }

    @Test
    @DisplayName("Should sort authors by bookCount descending and return highest count first")
    void shouldSortByBookCountDescending() {
        Page<AuthorBookCountDto> result = repository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookCount")));
        assertThat(result.getContent().get(0).lastName()).isEqualTo("Wilde");
        assertThat(result.getContent().get(0).bookCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should respect page size and return correct page metadata")
    void shouldRespectPageSizeAndReturnCorrectPageMetadata() {
        Page<AuthorBookCountDto> result = repository.findAll(
                PageRequest.of(0, 2, Sort.by("lastName")));
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should throw exception for unknown sort field")
    void shouldThrowExceptionForUnknownSortField() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> repository.findAll(PageRequest.of(0, 10, Sort.by("unknown"))));
    }
}
