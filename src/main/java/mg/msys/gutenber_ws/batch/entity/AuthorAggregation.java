package mg.msys.gutenber_ws.batch.entity;

import java.util.Map;
import java.util.Set;

public record AuthorAggregation(Map<String, Author> uniqueAuthors, Set<BookAuthorLink> links) {
}
