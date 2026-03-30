package mg.msys.gutenber_ws.batch.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.jdbc.core.JdbcTemplate;

import mg.msys.gutenber_ws.batch.task.CatalogBookMetadataParser.FacetEntry;
import mg.msys.gutenber_ws.batch.entity.Author;
import mg.msys.gutenber_ws.batch.entity.AuthorAggregation;
import mg.msys.gutenber_ws.batch.entity.BookAuthorLink;
import mg.msys.gutenber_ws.batch.entity.GutenbergBook;

public final class CatalogBookWriter implements ItemWriter<GutenbergBook> {
    private static final String UPSERT_AUTHOR_SQL = """
            INSERT INTO author (last_name, first_names, birth_year, death_year, normalized_key)
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT (normalized_key)
            DO UPDATE SET normalized_key = EXCLUDED.normalized_key
            RETURNING id
            """;

    private static final String INSERT_BOOK_AUTHOR_SQL = """
            INSERT INTO book_author (book_id, author_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String UPSERT_LANGUAGE_SQL = """
            INSERT INTO language (code)
            VALUES (?)
            ON CONFLICT (code)
            DO UPDATE SET code = EXCLUDED.code
            RETURNING id
            """;

    private static final String INSERT_BOOK_LANGUAGE_SQL = """
            INSERT INTO book_language (book_id, language_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    private static final String UPSERT_FACET_SQL = """
            INSERT INTO facet (code, label)
            VALUES (?, ?)
            ON CONFLICT (code)
            DO UPDATE SET label = EXCLUDED.label
            RETURNING id
            """;

    private static final String UPSERT_FACET_VALUE_SQL = """
            INSERT INTO facet_value (facet_id, raw_value, normalized_value)
            VALUES (?, ?, ?)
            ON CONFLICT (facet_id, normalized_value)
            DO UPDATE SET raw_value = EXCLUDED.raw_value
            RETURNING id
            """;

    private static final String INSERT_BOOK_FACET_VALUE_SQL = """
            INSERT INTO book_facet_value (book_id, facet_value_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING
            """;

    private final JdbcBatchItemWriter<GutenbergBook> bookWriter;
    private final JdbcTemplate jdbcTemplate;
    private final CatalogBookMetadataParser metadataParser;

    public CatalogBookWriter(JdbcBatchItemWriter<GutenbergBook> bookWriter, JdbcTemplate jdbcTemplate) {
        this(bookWriter, jdbcTemplate, new CatalogBookMetadataParser());
    }

    CatalogBookWriter(JdbcBatchItemWriter<GutenbergBook> bookWriter, JdbcTemplate jdbcTemplate,
            CatalogBookMetadataParser metadataParser) {
        this.bookWriter = bookWriter;
        this.jdbcTemplate = jdbcTemplate;
        this.metadataParser = metadataParser;
    }

    public static CatalogBookWriter create(DataSource dataSource) {
        JdbcBatchItemWriter<GutenbergBook> bookWriter = new JdbcBatchItemWriterBuilder<GutenbergBook>()
                .dataSource(dataSource)
                .sql("INSERT INTO book (id, title, issued, subjects) VALUES (:id, :title, :issued, :subjects) ON CONFLICT (id) DO NOTHING")
                .beanMapped()
                .assertUpdates(false)
                .build();
        bookWriter.afterPropertiesSet();

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return new CatalogBookWriter(bookWriter, jdbcTemplate);
    }

    @Override
    public void write(Chunk<? extends GutenbergBook> chunk) throws Exception {
        bookWriter.write(chunk);

        AuthorAggregation aggregation = collectAuthorsAndLinks(chunk);
        Map<String, Long> authorIdByKey = upsertAuthors(aggregation.uniqueAuthors());
        insertBookAuthorLinks(aggregation.links(), authorIdByKey);

        Map<Long, Set<String>> languageCodesByBook = collectLanguageCodesByBook(chunk);
        Map<String, Long> languageIdByCode = upsertLanguages(languageCodesByBook.values());
        insertBookLanguageLinks(languageCodesByBook, languageIdByCode);

        Map<Long, Set<FacetEntry>> facetsByBook = collectFacetsByBook(chunk);
        Map<String, Long> facetIdByCode = upsertFacets(facetsByBook.values());
        Map<String, Long> facetValueIdByKey = upsertFacetValues(facetsByBook.values(), facetIdByCode);
        insertBookFacetValueLinks(facetsByBook, facetValueIdByKey);
    }

    private AuthorAggregation collectAuthorsAndLinks(Chunk<? extends GutenbergBook> chunk) {
        Map<String, Author> uniqueAuthors = new LinkedHashMap<>();
        Set<BookAuthorLink> links = new LinkedHashSet<>();

        for (GutenbergBook book : chunk.getItems()) {
            for (Author authorRecord : metadataParser.parseAuthors(book.getAuthors())) {
                uniqueAuthors.putIfAbsent(authorRecord.normalizedKey(), authorRecord);
                links.add(new BookAuthorLink(book.getId(), authorRecord.normalizedKey()));
            }
        }

        return new AuthorAggregation(uniqueAuthors, links);
    }

    private Map<String, Long> upsertAuthors(Map<String, Author> uniqueAuthors) {
        Map<String, Long> authorIdByKey = new HashMap<>();

        for (Author authorRecord : uniqueAuthors.values()) {
            Long authorId = jdbcTemplate.queryForObject(
                    UPSERT_AUTHOR_SQL,
                    Long.class,
                    authorRecord.lastName(),
                    authorRecord.firstNames(),
                    authorRecord.birthYear(),
                    authorRecord.deathYear(),
                    authorRecord.normalizedKey());
            if (authorId != null) {
                authorIdByKey.put(authorRecord.normalizedKey(), authorId);
            }
        }

        return authorIdByKey;
    }

    private void insertBookAuthorLinks(Set<BookAuthorLink> links, Map<String, Long> authorIdByKey) {
        List<Object[]> linkParams = new ArrayList<>();

        for (BookAuthorLink link : links) {
            Long authorId = authorIdByKey.get(link.authorKey());
            if (authorId != null) {
                linkParams.add(new Object[] { link.bookId(), authorId });
            }
        }

        if (!linkParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_BOOK_AUTHOR_SQL, linkParams);
        }
    }

    private Map<Long, Set<String>> collectLanguageCodesByBook(Chunk<? extends GutenbergBook> chunk) {
        Map<Long, Set<String>> languageCodesByBook = new LinkedHashMap<>();

        for (GutenbergBook book : chunk.getItems()) {
            Set<String> codes = new LinkedHashSet<>(metadataParser.parseLanguageCodes(book.getLanguages()));
            if (!codes.isEmpty()) {
                languageCodesByBook.put(book.getId(), codes);
            }
        }

        return languageCodesByBook;
    }

    private Map<String, Long> upsertLanguages(Iterable<Set<String>> languageCodeSets) {
        Map<String, Long> languageIdByCode = new HashMap<>();
        Set<String> uniqueCodes = new LinkedHashSet<>();

        for (Set<String> codeSet : languageCodeSets) {
            uniqueCodes.addAll(codeSet);
        }

        for (String code : uniqueCodes) {
            Long languageId = jdbcTemplate.queryForObject(UPSERT_LANGUAGE_SQL, Long.class, code);
            if (languageId != null) {
                languageIdByCode.put(code, languageId);
            }
        }

        return languageIdByCode;
    }

    private void insertBookLanguageLinks(Map<Long, Set<String>> languageCodesByBook,
            Map<String, Long> languageIdByCode) {
        List<Object[]> linkParams = new ArrayList<>();
        Set<String> uniquePairs = new LinkedHashSet<>();

        for (Map.Entry<Long, Set<String>> entry : languageCodesByBook.entrySet()) {
            Long bookId = entry.getKey();
            for (String code : entry.getValue()) {
                Long languageId = languageIdByCode.get(code);
                if (languageId != null) {
                    String pairKey = bookId + "|" + languageId;
                    if (uniquePairs.add(pairKey)) {
                        linkParams.add(new Object[] { bookId, languageId });
                    }
                }
            }
        }

        if (!linkParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_BOOK_LANGUAGE_SQL, linkParams);
        }
    }

    private Map<Long, Set<FacetEntry>> collectFacetsByBook(Chunk<? extends GutenbergBook> chunk) {
        Map<Long, Set<FacetEntry>> facetsByBook = new LinkedHashMap<>();

        for (GutenbergBook book : chunk.getItems()) {
            Set<FacetEntry> facets = new LinkedHashSet<>(
                    metadataParser.parseBookshelves(book.getBookshelves()));
            if (!facets.isEmpty()) {
                facetsByBook.put(book.getId(), facets);
            }
        }

        return facetsByBook;
    }

    private Map<String, Long> upsertFacets(Iterable<Set<FacetEntry>> facetSets) {
        Set<String> facetCodes = new LinkedHashSet<>();
        for (Set<FacetEntry> facets : facetSets) {
            for (FacetEntry facet : facets) {
                facetCodes.add(facet.facetCode());
            }
        }

        Map<String, Long> facetIdByCode = new HashMap<>();
        for (String facetCode : facetCodes) {
            Long facetId = jdbcTemplate.queryForObject(
                    UPSERT_FACET_SQL,
                    Long.class,
                    facetCode,
                    defaultFacetLabel(facetCode));
            if (facetId != null) {
                facetIdByCode.put(facetCode, facetId);
            }
        }

        return facetIdByCode;
    }

    private Map<String, Long> upsertFacetValues(Iterable<Set<FacetEntry>> facetSets,
            Map<String, Long> facetIdByCode) {
        Set<FacetEntry> uniqueFacets = new LinkedHashSet<>();
        for (Set<FacetEntry> facets : facetSets) {
            uniqueFacets.addAll(facets);
        }

        Map<String, Long> facetValueIdByKey = new HashMap<>();
        for (FacetEntry facet : uniqueFacets) {
            Long facetId = facetIdByCode.get(facet.facetCode());
            if (facetId == null) {
                continue;
            }

            Long facetValueId = jdbcTemplate.queryForObject(
                    UPSERT_FACET_VALUE_SQL,
                    Long.class,
                    facetId,
                    facet.rawValue(),
                    facet.normalizedValue());
            if (facetValueId != null) {
                facetValueIdByKey.put(facetKey(facet.facetCode(), facet.normalizedValue()), facetValueId);
            }
        }

        return facetValueIdByKey;
    }

    private void insertBookFacetValueLinks(Map<Long, Set<FacetEntry>> facetsByBook,
            Map<String, Long> facetValueIdByKey) {
        List<Object[]> linkParams = new ArrayList<>();
        Set<String> uniquePairs = new LinkedHashSet<>();

        for (Map.Entry<Long, Set<FacetEntry>> entry : facetsByBook.entrySet()) {
            Long bookId = entry.getKey();
            for (FacetEntry facet : entry.getValue()) {
                Long facetValueId = facetValueIdByKey.get(facetKey(facet.facetCode(), facet.normalizedValue()));
                if (facetValueId != null) {
                    String pairKey = bookId + "|" + facetValueId;
                    if (uniquePairs.add(pairKey)) {
                        linkParams.add(new Object[] { bookId, facetValueId });
                    }
                }
            }
        }

        if (!linkParams.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_BOOK_FACET_VALUE_SQL, linkParams);
        }
    }

    private static String defaultFacetLabel(String facetCode) {
        return switch (facetCode) {
            case "category" -> "Category";
            case "topic" -> "Topic";
            default -> facetCode;
        };
    }

    private static String facetKey(String facetCode, String normalizedValue) {
        return facetCode + "|" + normalizedValue;
    }

}
