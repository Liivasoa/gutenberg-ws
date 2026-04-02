package mg.msys.gutenber_ws.book.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import mg.msys.gutenber_ws.book.dto.BookDetailDto;
import mg.msys.gutenber_ws.book.query.BookFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

@Repository
public class BookQueryJdbcRepository implements BookQueryRepository {

    private static final Map<String, String> COLUMN_MAP = Map.of(
            "title", "title",
            "id", "id");
    private final JdbcTemplate jdbc;

    public BookQueryJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page<BookDetailDto> findBooks(BookFilter filter, Pageable pageable) {
        List<Object> params = new ArrayList<>();
        String joins = buildJoins(filter, params);
        String where = buildWhere(filter, params);

        // Phase 1 — paginated IDs
        String countSql = "SELECT COUNT(DISTINCT b.id) FROM book b" + joins + where;
        long total = Objects.requireNonNull(jdbc.queryForObject(countSql, Long.class, params.toArray()));

        List<Object> pageParams = new ArrayList<>(params);
        pageParams.add(pageable.getPageSize());
        pageParams.add(pageable.getOffset());
        String idSql = "SELECT id FROM (SELECT DISTINCT b.id, b.title FROM book b" + joins + where + ") _sub"
                + buildOrderClause(pageable.getSort()) + " LIMIT ? OFFSET ?";
        List<Long> ids = jdbc.queryForList(idSql, Long.class, pageParams.toArray());

        if (ids.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        // Phase 2 — hydrate
        String inClause = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        Map<Long, String> titleById = fetchTitles(inClause);
        Map<Long, List<String>> authorsByBook = fetchAuthors(inClause);
        Map<Long, List<String>> languagesByBook = fetchLanguages(inClause);
        Map<Long, List<String>> categoriesByBook = fetchFacetValues(inClause, "category");
        Map<Long, List<String>> topicsByBook = fetchFacetValues(inClause, "topic");

        List<BookDetailDto> content = ids.stream()
                .map(id -> new BookDetailDto(
                        id,
                        titleById.getOrDefault(id, ""),
                        authorsByBook.getOrDefault(id, List.of()),
                        languagesByBook.getOrDefault(id, List.of()),
                        categoriesByBook.getOrDefault(id, List.of()),
                        topicsByBook.getOrDefault(id, List.of())))
                .toList();

        return new PageImpl<>(content, pageable, total);
    }

    private String buildJoins(BookFilter filter, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (filter.language() != null) {
            sb.append(" JOIN book_language bl ON b.id = bl.book_id")
                    .append(" JOIN language l ON bl.language_id = l.id");
        }
        if (filter.topicId() != null) {
            sb.append(" JOIN book_facet_value bfv_t ON b.id = bfv_t.book_id AND bfv_t.facet_value_id = ?");
            params.add(filter.topicId());
        }
        if (filter.categoryId() != null) {
            sb.append(" JOIN book_facet_value bfv_c ON b.id = bfv_c.book_id AND bfv_c.facet_value_id = ?");
            params.add(filter.categoryId());
        }
        if (filter.authorId() != null) {
            sb.append(" JOIN book_author ba ON b.id = ba.book_id AND ba.author_id = ?");
            params.add(filter.authorId());
        }
        return sb.toString();
    }

    private String buildWhere(BookFilter filter, List<Object> params) {
        StringBuilder sb = new StringBuilder();
        if (filter.language() != null) {
            sb.append(" WHERE l.code = ?");
            params.add(filter.language());
        }
        if (filter.bookId() != null) {
            sb.append(sb.isEmpty() ? " WHERE" : " AND").append(" b.id = ?");
            params.add(filter.bookId());
        }
        return sb.toString();
    }

    private String buildOrderClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY b.title ASC";
        }
        String parts = sort.stream()
                .map(order -> {
                    String col = COLUMN_MAP.get(order.getProperty());
                    if (col == null) {
                        throw new IllegalArgumentException("Unmapped sort field: " + order.getProperty());
                    }
                    return col + " " + order.getDirection().name();
                })
                .collect(Collectors.joining(", "));
        return " ORDER BY " + parts;
    }

    private Map<Long, String> fetchTitles(String inClause) {
        Map<Long, String> result = new HashMap<>();
        jdbc.query("SELECT id, title FROM book WHERE id IN (" + inClause + ")",
                (RowCallbackHandler) rs -> result.put(rs.getLong("id"), rs.getString("title")));
        return result;
    }

    private Map<Long, List<String>> fetchAuthors(String inClause) {
        Map<Long, List<String>> result = new HashMap<>();
        jdbc.query("""
                SELECT ba.book_id,
                       COALESCE(a.first_names || ' ', '') || UPPER(a.last_name) AS display_name
                FROM book_author ba
                JOIN author a ON ba.author_id = a.id
                WHERE ba.book_id IN (""" + inClause + ")",
                (RowCallbackHandler) rs -> result.computeIfAbsent(rs.getLong("book_id"), k -> new ArrayList<>())
                        .add(rs.getString("display_name")));
        return result;
    }

    private Map<Long, List<String>> fetchLanguages(String inClause) {
        Map<Long, List<String>> result = new HashMap<>();
        jdbc.query("""
                SELECT bl.book_id, l.code
                FROM book_language bl
                JOIN language l ON bl.language_id = l.id
                WHERE bl.book_id IN (""" + inClause + ")",
                (RowCallbackHandler) rs -> result.computeIfAbsent(rs.getLong("book_id"), k -> new ArrayList<>())
                        .add(rs.getString("code")));
        return result;
    }

    private Map<Long, List<String>> fetchFacetValues(String inClause, String facetCode) {
        Map<Long, List<String>> result = new HashMap<>();
        jdbc.query("""
                SELECT bfv.book_id, fv.raw_value
                FROM book_facet_value bfv
                JOIN facet_value fv ON bfv.facet_value_id = fv.id
                JOIN facet f ON fv.facet_id = f.id
                WHERE f.code = ? AND bfv.book_id IN (""" + inClause + ")",
                (RowCallbackHandler) rs -> result.computeIfAbsent(rs.getLong("book_id"), k -> new ArrayList<>())
                        .add(rs.getString("raw_value")),
                facetCode);
        return result;
    }
}
