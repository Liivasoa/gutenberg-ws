CREATE MATERIALIZED VIEW IF NOT EXISTS facet_value_book_count_mv AS
SELECT f.code              AS facet_code,
       fv.raw_value        AS value,
       fv.normalized_value AS normalized_value,
       COUNT(bfv.book_id)  AS book_count
FROM facet f
    JOIN facet_value fv ON f.id = fv.facet_id
    LEFT JOIN book_facet_value bfv ON fv.id = bfv.facet_value_id
GROUP BY f.code, fv.raw_value, fv.normalized_value;

CREATE UNIQUE INDEX IF NOT EXISTS idx_facet_value_book_count_mv_facet_code_normalized
    ON facet_value_book_count_mv (facet_code, normalized_value);
