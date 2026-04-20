package mg.msys.gutenber_ws.webservice.facet.repository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import mg.msys.gutenber_ws.webservice.facet.dto.FacetValueBookCountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FacetValueBookCountJdbcRepository implements FacetValueBookCountRepository {

    private static final Map<String, String> COLUMN_MAP = Map.of(
            "value", "value",
            "bookCount", "book_count");

    private static final String COUNT_SQL = "SELECT COUNT(*) FROM facet_value_book_count_mv WHERE facet_code = ?";

    private static final String SELECT_SQL = "SELECT id, value, book_count FROM facet_value_book_count_mv WHERE facet_code = ?";

    private final JdbcTemplate jdbc;

    public FacetValueBookCountJdbcRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Page<FacetValueBookCountDto> findAllByFacetCode(String facetCode, Pageable pageable) {
        long total = Objects.requireNonNull(jdbc.queryForObject(COUNT_SQL, Long.class, facetCode));

        String sql = SELECT_SQL + buildOrderClause(pageable.getSort()) + " LIMIT ? OFFSET ?";

        List<FacetValueBookCountDto> rows = jdbc.query(
                sql,
                (rs, rowNum) -> new FacetValueBookCountDto(rs.getLong("id"), rs.getString("value"),
                        rs.getLong("book_count")),
                facetCode,
                pageable.getPageSize(),
                pageable.getOffset());

        return new PageImpl<>(rows, pageable, total);
    }

    private String buildOrderClause(Sort sort) {
        if (sort.isUnsorted()) {
            return " ORDER BY value ASC";
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
