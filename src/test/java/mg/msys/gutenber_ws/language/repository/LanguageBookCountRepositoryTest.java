package mg.msys.gutenber_ws.language.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import mg.msys.gutenber_ws.language.dto.LanguageBookCountDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@ActiveProfiles("test")
@JdbcTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("LanguageBookCountRepository - Pagination and Sorting")
class LanguageBookCountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18.1-alpine");

    private LanguageBookCountRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setUp() {
        repository = new LanguageBookCountJdbcRepository(jdbcTemplate);
    }

    @BeforeAll
    void populateMv() {
        jdbcTemplate.update("INSERT INTO language (code) VALUES ('en') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO language (code) VALUES ('fr') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO language (code) VALUES ('de') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (1, 'Book 1') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (2, 'Book 2') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (3, 'Book 3') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 1, id FROM language WHERE code = 'en' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 2, id FROM language WHERE code = 'en' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 1, id FROM language WHERE code = 'fr' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 3, id FROM language WHERE code = 'de' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW language_book_count_mv");
    }

    @Test
    @DisplayName("Should return paginated language book counts with correct totals")
    void shouldReturnPaginatedLanguageBookCountsWithCorrectTotals() {
        Page<LanguageBookCountDto> result = repository.findAll(PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should sort by code ascending")
    void shouldSortByCodeAscending() {
        Page<LanguageBookCountDto> result = repository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "code")));

        List<String> codes = result.getContent().stream().map(LanguageBookCountDto::code).toList();
        assertThat(codes).containsExactly("de", "en", "fr");
    }

    @Test
    @DisplayName("Should sort by bookCount descending and return highest count first")
    void shouldSortByBookCountDescending() {
        Page<LanguageBookCountDto> result = repository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookCount")));

        assertThat(result.getContent().get(0).code()).isEqualTo("en");
        assertThat(result.getContent().get(0).bookCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should respect page size and return correct page metadata")
    void shouldRespectPageSizeAndReturnCorrectPageMetadata() {
        Page<LanguageBookCountDto> result = repository.findAll(
                PageRequest.of(0, 2, Sort.by("code")));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
