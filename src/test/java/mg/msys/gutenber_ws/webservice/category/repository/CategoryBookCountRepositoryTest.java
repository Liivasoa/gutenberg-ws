package mg.msys.gutenber_ws.webservice.category.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import mg.msys.gutenber_ws.webservice.facet.repository.FacetValueBookCountJdbcRepository;
import mg.msys.gutenber_ws.webservice.shared.repository.AbstractRepositoryTest;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("FacetValueBookCountRepository (category) - Pagination and Sorting")
class CategoryBookCountRepositoryTest extends AbstractRepositoryTest {

    private FacetValueBookCountJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void setUp() {
        repository = new FacetValueBookCountJdbcRepository(jdbcTemplate);
    }

    @BeforeAll
    void seedAndRefresh() {
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (10, 'C1') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (11, 'C2') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (12, 'C3') ON CONFLICT DO NOTHING");

        jdbcTemplate.update("INSERT INTO facet (code, label) VALUES ('category', 'Category') ON CONFLICT DO NOTHING");
        jdbcTemplate
                .update("""
                        INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                        SELECT id, 'Science Fiction', 'science fiction' FROM facet WHERE code = 'category' ON CONFLICT DO NOTHING
                        """);
        jdbcTemplate.update("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Adventure', 'adventure' FROM facet WHERE code = 'category' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Mystery', 'mystery' FROM facet WHERE code = 'category' ON CONFLICT DO NOTHING
                """);
        // Science Fiction → books 10 & 11 (count = 2), Adventure → book 10 (count = 1),
        // Mystery → book 12 (count = 1)
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 10, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'category' AND fv.normalized_value = 'science fiction' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 11, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'category' AND fv.normalized_value = 'science fiction' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 10, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'category' AND fv.normalized_value = 'adventure' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_facet_value (book_id, facet_value_id)
                SELECT 12, fv.id FROM facet_value fv JOIN facet f ON f.id = fv.facet_id
                WHERE f.code = 'category' AND fv.normalized_value = 'mystery' ON CONFLICT DO NOTHING
                """);

        jdbcTemplate.execute("REFRESH MATERIALIZED VIEW facet_value_book_count_mv");
    }

    @Test
    @DisplayName("Should return paginated categories with correct totals")
    void shouldReturnPaginatedCategoriesWithCorrectTotals() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("category", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(3);
    }

    @Test
    @DisplayName("Should sort categories by value ascending")
    void shouldSortByValueAscending() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("category",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "value")));

        List<String> values = result.getContent().stream().map(FacetValueBookCountDto::value).toList();
        assertThat(values).containsExactly("Adventure", "Mystery", "Science Fiction");
    }

    @Test
    @DisplayName("Should sort categories by bookCount descending and return highest count first")
    void shouldSortByBookCountDescending() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("category",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "bookCount")));

        assertThat(result.getContent().get(0).value()).isEqualTo("Science Fiction");
        assertThat(result.getContent().get(0).bookCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should respect page size and return correct page metadata")
    void shouldRespectPageSizeAndReturnCorrectPageMetadata() {
        Page<FacetValueBookCountDto> result = repository.findAllByFacetCode("category",
                PageRequest.of(0, 2, Sort.by("value")));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}
