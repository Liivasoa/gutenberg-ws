package mg.msys.gutenber_ws.batch.task;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mg.msys.gutenber_ws.batch.entity.Author;

final class CatalogBookMetadataParser {
    private static final Pattern AUTHOR_YEARS_PATTERN = Pattern.compile(
            "^(.*?)(?:,\\s*)?([0-9]{1,4})\\s*-\\s*([0-9]{0,4})\\s*$",
            Pattern.CASE_INSENSITIVE);

    List<Author> parseAuthors(String csvAuthors) {
        if (csvAuthors == null || csvAuthors.isBlank()) {
            return List.of();
        }

        return Arrays.stream(csvAuthors.split(";"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::parseSingleAuthor)
                .toList();
    }

    List<String> parseLanguageCodes(String rawLanguages) {
        if (rawLanguages == null || rawLanguages.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawLanguages.split(";"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    List<FacetEntry> parseBookshelves(String rawBookshelves) {
        if (rawBookshelves == null || rawBookshelves.isBlank()) {
            return List.of();
        }

        return Arrays.stream(rawBookshelves.split(";"))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::toFacetEntry)
                .filter(entry -> !entry.normalizedValue().isBlank())
                .toList();
    }

    String normalize(String value) {
        if (value == null) {
            return "";
        }

        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    Integer parseYear(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String value = raw.trim();
        if (!value.matches("[0-9]{1,4}")) {
            return null;
        }
        return Integer.parseInt(value);
    }

    private Author parseSingleAuthor(String rawAuthor) {
        String value = rawAuthor.trim();
        Integer birthYear = null;
        Integer deathYear = null;

        Matcher yearsMatcher = AUTHOR_YEARS_PATTERN.matcher(value);

        if (yearsMatcher.matches()) {
            value = yearsMatcher.group(1).trim();
            birthYear = parseYear(yearsMatcher.group(2).trim());
            String parsedDeathYear = yearsMatcher.group(3).trim();
            deathYear = parsedDeathYear.isBlank() ? null : parseYear(parsedDeathYear);
        }

        String lastName = value;
        String firstNames = null;
        int firstComma = value.indexOf(',');
        if (firstComma >= 0) {
            lastName = value.substring(0, firstComma).trim();
            firstNames = value.substring(firstComma + 1).trim();
            if (firstNames.isBlank()) {
                firstNames = null;
            }
        }

        String normalizedKey = normalize(lastName) + "|" + normalize(firstNames) + "|" + normalizeYear(birthYear) + "|"
                + normalizeYear(deathYear);

        return new Author(lastName, firstNames, birthYear, deathYear, normalizedKey);
    }

    private FacetEntry toFacetEntry(String rawBookshelfValue) {
        String value = rawBookshelfValue.trim();

        String facetCode = "topic";
        String facetValue = value;

        int separatorIndex = value.indexOf(':');
        if (separatorIndex > 0) {
            String prefix = value.substring(0, separatorIndex).trim();
            String remainder = value.substring(separatorIndex + 1).trim();
            if (prefix.equalsIgnoreCase("category")) {
                if (remainder.isBlank()) {
                    return new FacetEntry("topic", "", "");
                }
                facetCode = "category";
                facetValue = remainder;
            }
        }

        return new FacetEntry(facetCode, facetValue, normalize(facetValue));
    }

    private String normalizeYear(Integer year) {
        return year != null ? String.valueOf(year) : "";
    }

    static record FacetEntry(String facetCode, String rawValue, String normalizedValue) {
    }
}