CREATE INDEX IF NOT EXISTS idx_book_title ON book (title);
CREATE INDEX IF NOT EXISTS idx_book_language_language_id ON book_language (language_id);
CREATE INDEX IF NOT EXISTS idx_book_facet_value_facet_value_id ON book_facet_value (facet_value_id);
CREATE INDEX IF NOT EXISTS idx_book_author_author_id ON book_author (author_id);
