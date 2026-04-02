package mg.msys.gutenber_ws.topic.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import mg.msys.gutenber_ws.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.facet.repository.FacetValueBookCountJdbcRepository;
import mg.msys.gutenber_ws.shared.repository.AbstractRepositoryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("FacetValueBookCountRepository (topic) - Pagination and Sorting")
class TopicBookCountRepositoryTest extends AbstractRepositoryTest {

    private FacetValueBookCountJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setUp() {
        repository = new FacetValueBookCountJdbcRepository(jdbcTemplate);
    }

    @BeforeAll
    void seedAndRefresh() {
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (1, 'B1') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (2, 'B2') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (3, 'B3') ON CONFLICT DO NOTHING");

        jdbcTemplate.update("INSERT INTO facet (code, label) VALUES ('topic', 'Topic') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Fiction', 'fiction' FROM facet WHERE code = 'topic' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'History', 'history' FROM facet WHERE code = 'topic' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Science', 'science' FROM facet WHERE code = 'topic' ON CONFLICT DO NOTHING
                """);
        // Fiction → books 1 & 2 (count = 2), History → book 1 (count = 1), Science →
        // book 3 (count = 1)
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 1, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'topic' AND fv.normalized_value = 'fiction' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 2, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'topic' AND fv.normalized_value = 'fiction' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 1, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'topic' AND fv.normalized_value = 'history' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 3, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'topic' AND fv.normalized_value = 'science' ON CONFLICT DO NOTHING
                """);

        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW facet_value_book_count_mv");
    }

    @Test
    @DisplayName("Should return paginated topics with correct totals")
    void shouldReturnPaginatedTopicsWithCorrectTotals() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("topic", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should sort topics by value ascending")
    void shouldSortByValueAscending() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("topic",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "value")));

        List<String> values = result.getContent().stream().map(FacetValueBookCountDto::value).toList();
        assertThat(values).containsExactly("Fiction", "History", "Science");
    }

    @Test
    @DisplayName("Should sort topics by bookCount descending and return highest count first")
    void shouldSortByBookCountDescending() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("topic",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookCount")));

        assertThat(result.getContent().get(0).value()).isEqualTo("Fiction");
        assertThat(result.getContent().get(0).bookCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should respect page size and return correct page metadata")
    void shouldRespectPageSizeAndReturnCorrectPageMetadata() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("topic",
                PageRequest.of(0, 2, Sort.by("value")));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
