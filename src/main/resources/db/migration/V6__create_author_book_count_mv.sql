-- ============================================================================
-- Materialized View: author_book_count_mv
-- Precomputes per-author book counts for fast read-model queries.
-- Refreshed after each successful catalog import job.
-- ============================================================================

CREATE MATERIALIZED VIEW IF NOT EXISTS author_book_count_mv AS
SELECT a.id                   AS id,
       a.last_name             AS last_name,
       a.first_names           AS first_names,
       COUNT(ba.book_id)       AS book_count
FROM author a
         LEFT JOIN book_author ba ON a.id = ba.author_id
GROUP BY a.id, a.last_name, a.first_names;

CREATE UNIQUE INDEX IF NOT EXISTS idx_author_book_count_mv_id
    ON author_book_count_mv (id);
