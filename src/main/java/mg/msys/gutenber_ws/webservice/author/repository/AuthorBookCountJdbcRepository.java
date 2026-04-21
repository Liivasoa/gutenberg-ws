package mg.msys.gutenber_ws.webservice.author.repository;

import mg.msys.gutenber_ws.webservice.author.dto.AuthorBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
class AuthorBookCountJdbcRepository implements AuthorBookCountRepository {

    private static final Map<String, String> COLUMN_MAP = Map.of(
            "lastName", "last_name",
            "firstNames", "first_names",
            "bookCount", "book_count");

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM author_book_count_mv";
    private static final String SELECT_SQL = "SELECT id, last_name, first_names, book_count FROM author_book_count_mv";

    private final JdbcTemplate jdbc;

    AuthorBookCountJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page<AuthorBookCountDto> findAll(Pageable pageable) {
        long total = Objects.requireNonNull(jdbc.queryForObject(COUNT_SQL, Long.class));
        String sql = SELECT_SQL + buildOrderClause(pageable.getSort()) + " LIMIT ? OFFSET ?";
        List<AuthorBookCountDto> rows = jdbc.query(sql,
                (rs, rowNum) -> new AuthorBookCountDto(
                        rs.getLong("id"),
                        rs.getString("last_name"),
                        rs.getString("first_names"),
                        rs.getLong("book_count")),
                pageable.getPageSize(), pageable.getOffset());
        return new PageImpl<>(rows, pageable, total);
    }

    private String buildOrderClause(Sort sort) {
        if (sort.isUnsorted())
            return " ORDER BY last_name ASC";
        String parts = sort.stream()
                .map(order -> {
                    String col = COLUMN_MAP.get(order.getProperty());
                    if (col == null)
                        throw new IllegalArgumentException("Unmapped sort field: " + order.getProperty());
                    return col + " " + order.getDirection().name();
                })
                .collect(Collectors.joining(", "));
        return " ORDER BY " + parts;
    }
}
