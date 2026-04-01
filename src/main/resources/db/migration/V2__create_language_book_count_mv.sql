-- ============================================================================
-- Materialized View: language_book_count_mv
-- Precomputes per-language book counts for fast read-model queries.
-- Refreshed after each successful catalog import job.
-- ============================================================================

CREATE MATERIALIZED VIEW IF NOT EXISTS language_book_count_mv AS
SELECT l.code                 AS code,
       COUNT(bl.book_id)      AS book_count
FROM language l
         LEFT JOIN book_language bl ON l.id = bl.language_id
GROUP BY l.code;

CREATE UNIQUE INDEX IF NOT EXISTS idx_language_book_count_mv_code
    ON language_book_count_mv (code);
