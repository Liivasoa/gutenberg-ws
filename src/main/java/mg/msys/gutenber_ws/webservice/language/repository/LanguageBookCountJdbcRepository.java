package mg.msys.gutenber_ws.webservice.language.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import mg.msys.gutenber_ws.webservice.language.dto.LanguageBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
class LanguageBookCountJdbcRepository implements LanguageBookCountRepository {

    private static final Map<String, String> COLUMN_MAP = Map.of(
            "code", "code",
            "bookCount", "book_count");

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM language_book_count_mv";

    private static final String SELECT_SQL = "SELECT code, book_count FROM language_book_count_mv";

    private final JdbcTemplate jdbc;

    LanguageBookCountJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page<LanguageBookCountDto> findAll(Pageable pageable) {
        long total = Objects.requireNonNull(jdbc.queryForObject(COUNT_SQL, Long.class));

        String sql = SELECT_SQL + buildOrderClause(pageable.getSort()) + " LIMIT ? OFFSET ?";

        List<LanguageBookCountDto> rows = jdbc.query(
                sql,
                (rs, rowNum) -> new LanguageBookCountDto(rs.getString("code"), rs.getLong("book_count")),
                pageable.getPageSize(),
                pageable.getOffset());

        return new PageImpl<>(rows, pageable, total);
    }

    private String buildOrderClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY code ASC";
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
}
