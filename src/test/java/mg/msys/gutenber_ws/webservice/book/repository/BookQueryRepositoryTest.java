package mg.msys.gutenber_ws.webservice.book.repository;

import static org.assertj.core.api.Assertions.assertThat;

import mg.msys.gutenber_ws.webservice.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.webservice.book.query.BookFilter;
import mg.msys.gutenber_ws.webservice.shared.repository.AbstractRepositoryTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

@DisplayName("BookQueryRepository - Filters and Pagination")
class BookQueryRepositoryTest extends AbstractRepositoryTest {

    private BookQueryJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // IDs kept as fields to reference in filter tests
    private long authorId;
    private long topicFacetValueId;
    private long categoryFacetValueId;

    @BeforeAll
    void setUpAndSeedData() {
        repository = new BookQueryJdbcRepository(jdbcTemplate);

        // Purge book-related tables so data from other tests does not bleed in
        jdbcTemplate.update("DELETE FROM book_facet_value");
        jdbcTemplate.update("DELETE FROM book_language");
        jdbcTemplate.update("DELETE FROM book_author");
        jdbcTemplate.update("DELETE FROM book");
        jdbcTemplate.update("""
                DELETE FROM facet_value WHERE normalized_value IN ('adventure', 'classic')
                """);

        // Books
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (100, 'Moby Dick')");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (101, 'Alice in Wonderland')");
        jdbcTemplate.update("INSERT INTO book (id, title) VALUES (102, 'Les Misérables')");

        // Languages
        jdbcTemplate.update("INSERT INTO language (code) VALUES ('en') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO language (code) VALUES ('fr') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 100, id FROM language WHERE code = 'en' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 101, id FROM language WHERE code = 'en' ON CONFLICT DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO book_language (book_id, language_id)
                SELECT 102, id FROM language WHERE code = 'fr' ON CONFLICT DO NOTHING
                """);

        // Authors
        authorId = jdbcTemplate.queryForObject("""
                INSERT INTO author (last_name, first_names, normalized_key)
                VALUES ('Melville', 'Herman', 'melville|herman||')
                ON CONFLICT (normalized_key) DO UPDATE SET normalized_key = EXCLUDED.normalized_key
                RETURNING id
                """, Long.class);
        jdbcTemplate.update("""
                INSERT INTO book_author (book_id, author_id) VALUES (100, ?)
                ON CONFLICT DO NOTHING
                """, authorId);

        // Facets
        jdbcTemplate.update("INSERT INTO facet (code, label) VALUES ('topic', 'Topic') ON CONFLICT DO NOTHING");
        jdbcTemplate.update("INSERT INTO facet (code, label) VALUES ('category', 'Category') ON CONFLICT DO NOTHING");

        topicFacetValueId = jdbcTemplate.queryForObject("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Adventure', 'adventure' FROM facet WHERE code = 'topic'
                ON CONFLICT (facet_id, normalized_value) DO UPDATE SET raw_value = EXCLUDED.raw_value
                RETURNING id
                """, Long.class);
        categoryFacetValueId = jdbcTemplate.queryForObject("""
                INSERT INTO facet_value (facet_id, raw_value, normalized_value)
                SELECT id, 'Classic', 'classic' FROM facet WHERE code = 'category'
                ON CONFLICT (facet_id, normalized_value) DO UPDATE SET raw_value = EXCLUDED.raw_value
                RETURNING id
                """, Long.class);

        jdbcTemplate.update(
                "INSERT INTO book_facet_value (book_id, facet_value_id) VALUES (100, ?) ON CONFLICT DO NOTHING",
                topicFacetValueId);
        jdbcTemplate.update(
                "INSERT INTO book_facet_value (book_id, facet_value_id) VALUES (101, ?) ON CONFLICT DO NOTHING",
                topicFacetValueId);
        jdbcTemplate.update(
                "INSERT INTO book_facet_value (book_id, facet_value_id) VALUES (100, ?) ON CONFLICT DO NOTHING",
                categoryFacetValueId);
    }

    @AfterAll
    void tearDown() {
        jdbcTemplate.update("DELETE FROM book_facet_value");
        jdbcTemplate.update("DELETE FROM book_language");
        jdbcTemplate.update("DELETE FROM book_author");
        jdbcTemplate.update("DELETE FROM book");
        jdbcTemplate.update("""
                DELETE FROM facet_value WHERE normalized_value IN ('adventure', 'classic')
                """);
    }

    @Test
    @DisplayName("Should filter by language code")
    void shouldFilterByLanguage() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().language("en").build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(BookDetailDto::title)
                .containsExactly("Alice in Wonderland", "Moby Dick");
    }

    @Test
    @DisplayName("Should filter by topicId")
    void shouldFilterByTopicId() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().topicId(topicFacetValueId).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(BookDetailDto::id)
                .containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    @DisplayName("Should filter by categoryId")
    void shouldFilterByCategoryId() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().categoryId(categoryFacetValueId).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should filter by authorId")
    void shouldFilterByAuthorId() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().authorId(authorId).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).id()).isEqualTo(100L);
    }

    @Test
    @DisplayName("Should filter by bookId")
    void shouldFilterByBookId() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().bookId(101L).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).title()).isEqualTo("Alice in Wonderland");
    }

    @Test
    @DisplayName("Should combine language and topicId filters (AND)")
    void shouldCombineFilters() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().language("en").topicId(topicFacetValueId).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should populate authors, languages, categories, topics in result")
    void shouldPopulateAllFields() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().bookId(100L).build(),
                PageRequest.of(0, 10, Sort.by("title")));

        BookDetailDto book = result.getContent().get(0);
        assertThat(book.id()).isEqualTo(100L);
        assertThat(book.title()).isEqualTo("Moby Dick");
        assertThat(book.authors()).containsExactly("Herman MELVILLE");
        assertThat(book.languages()).containsExactly("en");
        assertThat(book.topics()).containsExactly("Adventure");
        assertThat(book.categories()).containsExactly("Classic");
    }

    @Test
    @DisplayName("Should sort by title descending")
    void shouldSortByTitleDescending() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().language("en").build(),
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title")));

        assertThat(result.getContent()).extracting(BookDetailDto::title)
                .containsExactly("Moby Dick", "Alice in Wonderland");
    }

    @Test
    @DisplayName("Should paginate correctly")
    void shouldPaginateCorrectly() {
        Page<BookDetailDto> result = repository.findBooks(
                BookFilter.builder().language("en").build(),
                PageRequest.of(0, 1, Sort.by("title")));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }
}
